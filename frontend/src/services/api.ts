import axios from 'axios';
import { ImageMetadata, TagRequest } from '../types';

const API_BASE_URL = 'http://localhost:8080';
const USER_ID = 'user-123'; // Hardcoded for now

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'X-User-Id': USER_ID,
  },
});

export const spectraApi = {
  uploadImage: async (file: File): Promise<ImageMetadata> => {
    const formData = new FormData();
    formData.append('file', file);
    
    const response = await api.post('/api/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response.data;
  },

  getAllImages: async (): Promise<ImageMetadata[]> => {
    const response = await api.get('/api/images');
    return response.data;
  },

  searchImages: async (tags: string[]): Promise<ImageMetadata[]> => {
    const response = await api.get(`/api/images/search?tags=${tags.join(',')}`);
    return response.data;
  },

  getImage: async (id: string): Promise<ImageMetadata> => {
    const response = await api.get(`/api/images/${id}`);
    return response.data;
  },

  addTags: async (id: string, tagRequest: TagRequest): Promise<ImageMetadata> => {
    const response = await api.post(`/api/images/${id}/metadata`, tagRequest);
    return response.data;
  },
};
