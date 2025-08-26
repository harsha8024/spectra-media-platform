export interface ImageMetadata {
  id: string;
  userId: string;
  originalFilename: string;
  storageUrl: string;
  thumbnailUrl: string;
  tags: string[];
  palette: string[];
  createdAt: string;
}

export interface TagRequest {
  tags: string[];
  palette: string[];
}

export interface ApiError {
  message: string;
  status?: number;
}
