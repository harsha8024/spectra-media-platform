import React from 'react';
import { useDropzone } from 'react-dropzone';
import { FiX, FiUploadCloud } from 'react-icons/fi';

interface UploadModalProps {
  isOpen: boolean;
  onClose: () => void;
  onUpload: (file: File) => Promise<void>;
}

export const UploadModal: React.FC<UploadModalProps> = ({
  isOpen,
  onClose,
  onUpload,
}) => {
  const [isUploading, setIsUploading] = React.useState(false);
  const [error, setError] = React.useState<string | null>(null);

  const onDrop = React.useCallback(
    async (acceptedFiles: File[]) => {
      const file = acceptedFiles[0];
      if (!file) return;

      setIsUploading(true);
      setError(null);

      try {
        await onUpload(file);
        onClose();
      } catch (err) {
        setError('Failed to upload image. Please try again.');
      } finally {
        setIsUploading(false);
      }
    },
    [onUpload, onClose]
  );

  const { getRootProps, getInputProps, isDragActive } = useDropzone({
    onDrop,
    accept: {
      'image/*': ['.png', '.jpg', '.jpeg', '.gif']
    },
    maxFiles: 1,
  });

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
      <div className="bg-white rounded-lg w-full max-w-lg p-6">
        <div className="flex justify-between items-center mb-6">
          <h2 className="text-xl font-semibold">Upload Image</h2>
          <button
            onClick={onClose}
            className="text-gray-500 hover:text-gray-700"
          >
            <FiX className="w-6 h-6" />
          </button>
        </div>

        <div
          {...getRootProps()}
          className={`border-2 border-dashed rounded-lg p-8 text-center cursor-pointer transition-colors
            ${isDragActive ? 'border-spectra-500 bg-spectra-50' : 'border-gray-300 hover:border-spectra-400'}`}
        >
          <input {...getInputProps()} />
          <FiUploadCloud className="w-12 h-12 mx-auto mb-4 text-spectra-500" />
          {isDragActive ? (
            <p className="text-spectra-600">Drop the image here</p>
          ) : (
            <div>
              <p className="text-gray-600 mb-2">
                Drag and drop an image here, or click to select
              </p>
              <p className="text-gray-400 text-sm">
                Supports: PNG, JPG, JPEG, GIF
              </p>
            </div>
          )}
        </div>

        {error && (
          <div className="mt-4 p-3 bg-red-50 text-red-700 rounded">
            {error}
          </div>
        )}

        {isUploading && (
          <div className="mt-4 flex items-center justify-center">
            <div className="animate-spin rounded-full h-8 w-8 border-4 border-spectra-500 border-t-transparent"></div>
            <span className="ml-3">Uploading...</span>
          </div>
        )}
      </div>
    </div>
  );
};
