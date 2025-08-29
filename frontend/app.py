import streamlit as st
import requests
import pandas as pd
from PIL import Image
from io import BytesIO
import time

API_URL = "http://spectra-api-gateway:8081"
REQUEST_TIMEOUT = 15  # seconds
POLL_INTERVAL = 2     # seconds
POLL_TIMEOUT = 180    # seconds (wait longer for metadata + tags)

# Configure page
st.set_page_config(page_title="Spectra", layout="wide", initial_sidebar_state="expanded")

# Dark theme
st.markdown("""
    <style>
        .stApp {
            background-color: #0E1117;
            color: #FAFAFA;
        }
        .uploadedFile {
            background-color: #262730;
            padding: 1rem;
            border-radius: 4px;
        }
        .stDataFrame {
            background-color: #262730;
        }
    </style>
""", unsafe_allow_html=True)

def upload_image(file):
    try:
        files = {'file': file}
        headers = {'X-User-Id': 'user-123'}
        response = requests.post(
            f"{API_URL}/api/images/upload",
            files=files,
            headers=headers,
            timeout=REQUEST_TIMEOUT
        )
        # Try to extract the imageId from the response body when 202 Accepted.
        image_id = None
        if response is not None and response.status_code == 202:
            # Response body is the UUID; strip quotes/whitespace just in case
            image_id = (response.text or "").strip().strip('"').strip()
        return response, image_id
    except requests.exceptions.ConnectionError:
        st.error("Could not connect to the API Gateway. Please ensure all services are running.")
        return None, None
    except requests.exceptions.Timeout:
        st.error("Request timed out. Please try again.")
        return None, None
    except Exception as e:
        st.error(f"Error uploading image: {str(e)}")
        return None, None

def get_image_data():
    try:
        response = requests.get(f"{API_URL}/api/images", timeout=REQUEST_TIMEOUT)
        if response.status_code == 200:
            return response.json()
        else:
            st.error(f"Failed to fetch images: {response.status_code}")
            return []
    except Exception as e:
        st.error(f"Error connecting to API: {str(e)}")
        return []

def display_image(image_id):
    try:
        response = requests.get(f"{API_URL}/api/images/content/{image_id}", timeout=REQUEST_TIMEOUT)
        if response.status_code == 200:
            image = Image.open(BytesIO(response.content))
            st.image(image, use_column_width=True)
        else:
            st.error(f"Failed to load image: {response.status_code}")
    except Exception as e:
        st.error(f"Error loading image: {str(e)}")

def poll_until_processed(image_id: str) -> bool:
    """Poll the API until the image appears, then until tags are generated (or timeout)."""
    if not image_id:
        return False

    status = st.empty()
    start = time.time()
    found = False
    tagged = False

    while time.time() - start < POLL_TIMEOUT:
        images = get_image_data()
        if images:
            # Find our image
            match = next((img for img in images if str(img.get('id')) == str(image_id)), None)
            if match:
                found = True
                # If tags are present and non-empty, we can stop; otherwise keep waiting a bit
                tags = match.get('tags') or []
                if tags:
                    tagged = True
                    status.success("Image processed and tags generated.")
                    return True
                else:
                    status.info("Image saved. Waiting for AI tags...")
            else:
                status.info("Waiting for image to appear...")
        else:
            status.info("Waiting for image list...")

        time.sleep(POLL_INTERVAL)

    # If we exit due to timeout, still refresh UI if image exists without tags
    if found and not tagged:
        status.warning("Image saved but tags not ready yet. Displaying current state.")
        return True

    status.error("Timed out waiting for image to be processed.")
    return False

def main():
    st.title("Spectra Image Management")

    # Sidebar for upload
    with st.sidebar:
        st.header("Upload Image")
        uploaded_file = st.file_uploader("Choose an image", type=['jpg', 'jpeg', 'png'])
        
        if uploaded_file:
            if st.button("Upload"):
                with st.spinner("Uploading..."):
                    response, image_id = upload_image(uploaded_file)
                    if response and response.status_code == 202 and image_id:
                        st.success(f"Image uploaded! ID: {image_id}")
                        # Poll until image shows up and (optionally) tags appear
                        if poll_until_processed(image_id):
                            st.rerun()  # Refresh the page after processing/polling
                    elif response:
                        st.error(f"Upload failed with status: {response.status_code}")
                    # If response is None, error was already displayed in upload_image()

    # Main content - Tabs for Gallery and Table view
    tab1, tab2 = st.tabs(["Gallery View", "Table View"])
    
    images = get_image_data()
    
    with tab1:
        cols = st.columns(3)
        for idx, img in enumerate(images):
            with cols[idx % 3]:
                with st.container():
                    display_image(img['id'])
                    st.caption(f"File: {img['originalFilename']}")
                    if img.get('tags'):
                        st.write("Tags:", ", ".join(img['tags']))
                    st.divider()

    with tab2:
        if images:
            # Convert to DataFrame
            df = pd.DataFrame([{
                'Image ID': img['id'],
                'Filename': img['originalFilename'],
                'Tags': ', '.join(img.get('tags') or []),
                'Colors': ', '.join(img.get('palette') or []),
                'Upload Date': img['createdAt']
            } for img in images])
            st.dataframe(df, use_container_width=True)
        else:
            st.info("No images found")

if __name__ == "__main__":
    main()
