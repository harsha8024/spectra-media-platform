import axios from 'axios';
import { ImageMetadata, TagRequest } from '../types';

// Use relative URL to leverage nginx proxy
const API_BASE_URL = '/api';

const api = axios.create({
    baseURL: API_BASE_URL,
    headers: {
        'Content-Type': 'application/json',
        'X-User-Id': 'user-123' // Default test user
    }
});

export const spectraApi = {
    uploadImage: async (file: File): Promise<ImageMetadata> => {
        const formData = new FormData();
        formData.append('file', file);
        
        const response = await api.post('/images/upload', formData, {
            headers: {
                'Content-Type': 'multipart/form-data',
            },
        });
        return response.data;
    },

    getAllImages: async (): Promise<ImageMetadata[]> => {
        const response = await api.get('/images');
        return response.data;
    },

    searchImages: async (tags: string[]): Promise<ImageMetadata[]> => {
        const response = await api.get(`/images/search?tags=${tags.join(',')}`);
        return response.data;
    },

    getImage: async (id: string): Promise<ImageMetadata> => {
        const response = await api.get(`/images/${id}`);
        return response.data;
    },

    addTags: async (id: string, tagRequest: TagRequest): Promise<ImageMetadata> => {
        const response = await api.post(`/images/${id}/metadata`, tagRequest);
        return response.data;
    },
};
