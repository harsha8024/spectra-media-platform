from fastapi import FastAPI
from consumer import RabbitMQConsumer
import uvicorn
import logging
from pythonjsonlogger import jsonlogger

# Configure JSON logging
logger = logging.getLogger(__name__)
logHandler = logging.StreamHandler()
formatter = jsonlogger.JsonFormatter(
    fmt='%(asctime)s %(levelname)s %(name)s %(message)s'
)
logHandler.setFormatter(formatter)
logger.addHandler(logHandler)
logger.setLevel(logging.INFO)

app = FastAPI(
    title="Spectra AI Tagging Service",
    description="AI-powered image tagging service for the Spectra Media Platform",
    version="1.0.0"
)

# Store the consumer instance
consumer = None

@app.on_event("startup")
async def startup_event():
    """Start the RabbitMQ consumer when the application starts."""
    global consumer
    logger.info("Starting RabbitMQ consumer")
    consumer = RabbitMQConsumer()
    consumer.daemon = True  # Allow the thread to be terminated with the main process
    consumer.start()

@app.on_event("shutdown")
async def shutdown_event():
    """Stop the RabbitMQ consumer when the application shuts down."""
    global consumer
    if consumer:
        logger.info("Stopping RabbitMQ consumer")
        consumer.stop()
        consumer.join()

@app.get("/health")
async def health_check():
    """Simple health check endpoint."""
    return {"status": "healthy"}

if __name__ == "__main__":
    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=False)
