import React from 'react';
import { ImageMetadata } from '../types';
import { Link } from 'react-router-dom';

interface GalleryViewProps {
  images: ImageMetadata[];
  isLoading: boolean;
  error: string | null;
}

export const GalleryView: React.FC<GalleryViewProps> = ({
  images,
  isLoading,
  error,
}) => {
  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-96">
        <div className="animate-spin rounded-full h-12 w-12 border-4 border-spectra-500 border-t-transparent"></div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="text-center p-8">
        <p className="text-red-600">{error}</p>
      </div>
    );
  }

  if (images.length === 0) {
    return (
      <div className="text-center p-8">
        <p className="text-gray-500">No images found</p>
      </div>
    );
  }

  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4 p-4">
      {images.map((image) => (
        <Link
          key={image.id}
          to={`/image/${image.id}`}
          className="block group relative aspect-square rounded-lg overflow-hidden bg-gray-100 hover:shadow-lg transition-shadow"
        >
          <img
            src={image.thumbnailUrl}
            alt={image.originalFilename}
            className="w-full h-full object-cover"
          />
          <div className="absolute bottom-0 left-0 right-0 bg-gradient-to-t from-black/50 to-transparent p-4 opacity-0 group-hover:opacity-100 transition-opacity">
            <div className="flex flex-wrap gap-1">
              {image.tags.slice(0, 3).map((tag) => (
                <span
                  key={tag}
                  className="text-xs px-2 py-1 bg-white/80 rounded-full text-gray-800"
                >
                  {tag}
                </span>
              ))}
              {image.tags.length > 3 && (
                <span className="text-xs px-2 py-1 bg-white/80 rounded-full text-gray-800">
                  +{image.tags.length - 3}
                </span>
              )}
            </div>
          </div>
        </Link>
      ))}
    </div>
  );
};
