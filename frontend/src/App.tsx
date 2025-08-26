import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { Sidebar } from './components/Sidebar';
import { GalleryView } from './components/GalleryView';
import { DetailView } from './components/DetailView';
import { SearchBar } from './components/SearchBar';
import { UploadModal } from './components/UploadModal';
import { ImageMetadata } from './types';
import { spectraApi } from './services/api';

export const App: React.FC = () => {
  const [isUploadModalOpen, setIsUploadModalOpen] = React.useState(false);
  const [images, setImages] = React.useState<ImageMetadata[]>([]);
  const [isLoading, setIsLoading] = React.useState(true);
  const [error, setError] = React.useState<string | null>(null);

  const fetchImages = React.useCallback(async () => {
    setIsLoading(true);
    setError(null);
    try {
      const data = await spectraApi.getAllImages();
      setImages(data);
    } catch (err) {
      setError('Failed to load images');
    } finally {
      setIsLoading(false);
    }
  }, []);

  React.useEffect(() => {
    fetchImages();
  }, [fetchImages]);

  const handleSearch = async (query: string) => {
    if (!query.trim()) {
      fetchImages();
      return;
    }

    setIsLoading(true);
    setError(null);
    try {
      const tags = query.split(',').map(tag => tag.trim());
      const data = await spectraApi.searchImages(tags);
      setImages(data);
    } catch (err) {
      setError('Failed to search images');
    } finally {
      setIsLoading(false);
    }
  };

  const handleUpload = async (file: File) => {
    await spectraApi.uploadImage(file);
    await fetchImages();
  };

  return (
    <BrowserRouter>
      <div className="min-h-screen bg-gray-50">
        <Sidebar onUploadClick={() => setIsUploadModalOpen(true)} />
        
        <main className="ml-64 min-h-screen">
          <div className="max-w-7xl mx-auto py-6 px-4 sm:px-6 lg:px-8">
            <Routes>
              <Route
                path="/"
                element={
                  <>
                    <div className="mb-6 flex justify-center">
                      <SearchBar onSearch={handleSearch} isLoading={isLoading} />
                    </div>
                    <GalleryView
                      images={images}
                      isLoading={isLoading}
                      error={error}
                    />
                  </>
                }
              />
              <Route path="/image/:id" element={<DetailView />} />
              <Route path="*" element={<Navigate to="/" replace />} />
            </Routes>
          </div>
        </main>

        <UploadModal
          isOpen={isUploadModalOpen}
          onClose={() => setIsUploadModalOpen(false)}
          onUpload={handleUpload}
        />
      </div>
    </BrowserRouter>
  );
};
