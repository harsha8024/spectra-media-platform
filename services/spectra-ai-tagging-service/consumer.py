import io
import json
import logging
import random
import threading
from typing import List, Tuple
from pathlib import Path

import numpy as np
import pika
import requests
from PIL import Image
from pythonjsonlogger import jsonlogger
from ultralytics import YOLO

# Configure JSON logging
logger = logging.getLogger(__name__)
logHandler = logging.StreamHandler()
formatter = jsonlogger.JsonFormatter(
    fmt='%(asctime)s %(levelname)s %(name)s %(message)s'
)
logHandler.setFormatter(formatter)
logger.addHandler(logHandler)
logger.setLevel(logging.INFO)

# Constants
RABBITMQ_HOST = 'rabbitmq'
EXCHANGE_NAME = 'spectra-exchange'
QUEUE_NAME = 'ai-tagging-queue'
ROUTING_KEY = 'image.processed'
TAG_MANAGER_URL = 'http://spectra-tag-manager:8080'
STORAGE_BASE_URL = 'http://some-object-storage-service'  # Replace with actual storage URL
MODEL_PATH = 'yolov8n.pt'

# Initialize YOLO model
try:
    model = YOLO(MODEL_PATH)
    logger.info(f"Successfully loaded YOLOv8 model from {MODEL_PATH}")
except Exception as e:
    logger.error(f"Failed to load YOLOv8 model: {e}")
    raise

def generate_random_color() -> str:
    """Generate a random hex color code."""
    return f"#{random.randint(0, 0xFFFFFF):06x}"

def download_image(url: str) -> Image.Image:
    """Download an image from a URL and return as PIL Image."""
    try:
        response = requests.get(url, stream=True)
        response.raise_for_status()
        image_data = io.BytesIO(response.content)
        return Image.open(image_data)
    except Exception as e:
        logger.error(f"Failed to download image from {url}: {e}")
        raise

def analyze_image(image: Image.Image) -> Tuple[List[str], List[str]]:
    """Perform object detection on the image and extract color palette."""
    try:
        # Convert PIL Image to numpy array for YOLO
        img_array = np.array(image)
        
        # Run inference
        results = model(img_array, verbose=False)[0]
        
        # Extract unique class names from predictions
        detected_classes = set()
        for result in results.boxes.data:
            class_id = int(result[5])  # Class ID is in the 6th column
            class_name = results.names[class_id]  # Get class name from ID
            detected_classes.add(class_name)
        
        # Convert set to list
        tags = list(detected_classes)
        
        # If no objects detected, add a generic tag
        if not tags:
            tags = ['unknown']
        
        # Generate random colors (this could be replaced with actual color extraction)
        num_colors = random.randint(3, 5)
        colors = [generate_random_color() for _ in range(num_colors)]
        
        return tags, colors
        
    except Exception as e:
        logger.error(f"Failed to analyze image: {e}")
        raise

def process_image(image_id: str, original_location: str) -> None:
    """Process an image and update its metadata via the tag manager API."""
    try:
        # Download the image from storage
        storage_url = f"{STORAGE_BASE_URL}/{original_location}"
        logger.info(f"Downloading image from {storage_url}")
        image = download_image(storage_url)

        # Analyze the image using YOLO
        tags, colors = analyze_image(image)
        logger.info(f"Generated tags and colors for image {image_id}", 
                   extra={"tags": tags, "colors": colors})

        # Prepare the request to the tag manager
        url = f"{TAG_MANAGER_URL}/api/images/{image_id}/metadata"
        payload = {
            "tags": tags,
            "palette": colors
        }

        # Make the API call
        response = requests.post(url, json=payload)
        response.raise_for_status()

        logger.info(f"Successfully updated metadata for image {image_id}")

    except requests.exceptions.RequestException as e:
        logger.error(f"Failed to update metadata for image {image_id}: {str(e)}")
    except Exception as e:
        logger.error(f"Error processing image {image_id}: {str(e)}")

class RabbitMQConsumer(threading.Thread):
    """A threaded RabbitMQ consumer."""
    
    def __init__(self):
        super().__init__()
        self.should_stop = False

    def run(self):
        """Main consumer loop."""
        while not self.should_stop:
            try:
                # Establish connection
                connection = pika.BlockingConnection(
                    pika.ConnectionParameters(host=RABBITMQ_HOST)
                )
                channel = connection.channel()

                # Declare exchange and queue
                channel.exchange_declare(
                    exchange=EXCHANGE_NAME,
                    exchange_type='topic',
                    durable=True
                )
                channel.queue_declare(queue=QUEUE_NAME, durable=True)
                channel.queue_bind(
                    exchange=EXCHANGE_NAME,
                    queue=QUEUE_NAME,
                    routing_key=ROUTING_KEY
                )

                logger.info(f"Connected to RabbitMQ, listening on queue {QUEUE_NAME}")

                def callback(ch, method, properties, body):
                    """Process incoming messages."""
                    try:
                        message = json.loads(body)
                        image_id = message['imageId']
                        original_location = message['originalLocation']
                        
                        logger.info(f"Received message for image {image_id}")
                        process_image(image_id, original_location)
                        
                        ch.basic_ack(delivery_tag=method.delivery_tag)
                    
                    except json.JSONDecodeError:
                        logger.error("Failed to decode message JSON")
                        ch.basic_nack(delivery_tag=method.delivery_tag)
                    except KeyError as e:
                        logger.error(f"Missing required field in message: {e}")
                        ch.basic_nack(delivery_tag=method.delivery_tag)
                    except Exception as e:
                        logger.error(f"Error processing message: {e}")
                        ch.basic_nack(delivery_tag=method.delivery_tag)

                # Start consuming
                channel.basic_qos(prefetch_count=1)
                channel.basic_consume(
                    queue=QUEUE_NAME,
                    on_message_callback=callback
                )
                channel.start_consuming()

            except pika.exceptions.AMQPConnectionError:
                logger.error("Lost connection to RabbitMQ, retrying...")
                continue
            except Exception as e:
                logger.error(f"Unexpected error: {e}")
                if not self.should_stop:
                    continue
                break

    def stop(self):
        """Stop the consumer thread."""
        self.should_stop = True
