import streamlit as st
import requests
import pandas as pd
from PIL import Image
from io import BytesIO
import time

API_URL = "http://spectra-api-gateway:8081"

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
    files = {'file': file}
    headers = {'X-User-Id': 'user-123'}
    response = requests.post(f"{API_URL}/api/images/upload", 
                           files=files, 
                           headers=headers)
    return response

def get_image_data():
    response = requests.get(f"{API_URL}/api/images")
    if response.status_code == 200:
        return response.json()
    return []

def display_image(image_id):
    try:
        response = requests.get(f"{API_URL}/api/images/content/{image_id}")
        if response.status_code == 200:
            image = Image.open(BytesIO(response.content))
            st.image(image, use_column_width=True)
    except Exception as e:
        st.error(f"Error loading image: {str(e)}")

def main():
    st.title("Spectra Image Management")

    # Sidebar for upload
    with st.sidebar:
        st.header("Upload Image")
        uploaded_file = st.file_uploader("Choose an image", type=['jpg', 'jpeg', 'png'])
        
        if uploaded_file:
            if st.button("Upload"):
                with st.spinner("Uploading..."):
                    response = upload_image(uploaded_file)
                    if response.status_code == 202:
                        st.success("Image uploaded! Processing...")
                        time.sleep(2)  # Wait for processing
                        st.rerun()  # Refresh the page
                    else:
                        st.error("Upload failed")

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
                    if img['tags']:
                        st.write("Tags:", ", ".join(img['tags']))
                    st.divider()

    with tab2:
        if images:
            # Convert to DataFrame
            df = pd.DataFrame([{
                'Image ID': img['id'],
                'Filename': img['originalFilename'],
                'Tags': ', '.join(img['tags']),
                'Upload Date': img['createdAt']
            } for img in images])
            st.dataframe(df, use_container_width=True)
        else:
            st.info("No images found")

if __name__ == "__main__":
    main()
