import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import { FiHome, FiSearch, FiUpload } from 'react-icons/fi';

interface SidebarProps {
  onUploadClick: () => void;
}

export const Sidebar: React.FC<SidebarProps> = ({ onUploadClick }) => {
  const location = useLocation();

  const isActive = (path: string) => location.pathname === path;

  return (
    <div className="w-64 h-screen bg-white border-r border-gray-200 fixed left-0 top-0">
      <div className="p-6">
        <h1 className="text-2xl font-bold text-spectra-600">Spectra</h1>
      </div>
      
      <nav className="mt-6">
        <Link
          to="/"
          className={`flex items-center px-6 py-3 text-gray-700 hover:bg-spectra-50 ${
            isActive('/') ? 'bg-spectra-50 text-spectra-600' : ''
          }`}
        >
          <FiHome className="w-5 h-5 mr-3" />
          All Photos
        </Link>
        
        <Link
          to="/search"
          className={`flex items-center px-6 py-3 text-gray-700 hover:bg-spectra-50 ${
            isActive('/search') ? 'bg-spectra-50 text-spectra-600' : ''
          }`}
        >
          <FiSearch className="w-5 h-5 mr-3" />
          Search
        </Link>

        <button
          onClick={onUploadClick}
          className="w-full flex items-center px-6 py-3 text-gray-700 hover:bg-spectra-50"
        >
          <FiUpload className="w-5 h-5 mr-3" />
          Upload
        </button>
      </nav>
    </div>
  );
};
