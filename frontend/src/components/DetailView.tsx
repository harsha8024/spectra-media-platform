import React from 'react';
import { useParams } from 'react-router-dom';
import { FiPlus } from 'react-icons/fi';
import { ImageMetadata } from '../types';
import { spectraApi } from '../services/api';

export const DetailView: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const [image, setImage] = React.useState<ImageMetadata | null>(null);
  const [isLoading, setIsLoading] = React.useState(true);
  const [error, setError] = React.useState<string | null>(null);
  const [newTag, setNewTag] = React.useState('');
  const [isAddingTag, setIsAddingTag] = React.useState(false);

  React.useEffect(() => {
    const fetchImage = async () => {
      if (!id) return;
      
      try {
        const data = await spectraApi.getImage(id);
        setImage(data);
      } catch (err) {
        setError('Failed to load image details');
      } finally {
        setIsLoading(false);
      }
    };

    fetchImage();
  }, [id]);

  const handleAddTag = async () => {
    if (!id || !image || !newTag.trim()) return;

    setIsAddingTag(true);
    try {
      const updatedImage = await spectraApi.addTags(id, {
        tags: [...image.tags, newTag.trim()],
        palette: image.palette,
      });
      setImage(updatedImage);
      setNewTag('');
    } catch (err) {
      setError('Failed to add tag');
    } finally {
      setIsAddingTag(false);
    }
  };

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-96">
        <div className="animate-spin rounded-full h-12 w-12 border-4 border-spectra-500 border-t-transparent"></div>
      </div>
    );
  }

  if (error || !image) {
    return (
      <div className="text-center p-8">
        <p className="text-red-600">{error || 'Image not found'}</p>
      </div>
    );
  }

  return (
    <div className="max-w-6xl mx-auto p-6">
      <div className="bg-white rounded-lg shadow-lg overflow-hidden">
        <div className="aspect-video relative">
          <img
            src={image.storageUrl}
            alt={image.originalFilename}
            className="w-full h-full object-contain"
          />
        </div>

        <div className="p-6">
          <h2 className="text-2xl font-semibold mb-4">{image.originalFilename}</h2>
          
          <div className="mb-6">
            <h3 className="text-lg font-medium mb-2">Color Palette</h3>
            <div className="flex gap-2">
              {image.palette.map((color) => (
                <div
                  key={color}
                  style={{ backgroundColor: color }}
                  className="w-12 h-12 rounded-lg shadow"
                  title={color}
                />
              ))}
            </div>
          </div>

          <div>
            <h3 className="text-lg font-medium mb-2">Tags</h3>
            <div className="flex flex-wrap gap-2 mb-4">
              {image.tags.map((tag) => (
                <span
                  key={tag}
                  className="px-3 py-1 bg-spectra-100 text-spectra-700 rounded-full text-sm"
                >
                  {tag}
                </span>
              ))}
            </div>

            <div className="flex gap-2">
              <input
                type="text"
                value={newTag}
                onChange={(e) => setNewTag(e.target.value)}
                placeholder="Add a new tag"
                className="flex-1 px-3 py-2 border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-spectra-500"
                onKeyPress={(e) => e.key === 'Enter' && handleAddTag()}
              />
              <button
                onClick={handleAddTag}
                disabled={isAddingTag || !newTag.trim()}
                className="px-4 py-2 bg-spectra-500 text-white rounded hover:bg-spectra-600 disabled:opacity-50 disabled:cursor-not-allowed flex items-center"
              >
                <FiPlus className="w-5 h-5 mr-1" />
                Add
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
