import type { Application } from '../data/models';

interface Props {
  app: Application;
  onClick: () => void;
  isEditing?: boolean;
  onDelete?: () => void;
  onEdit?: () => void;
}

export const ApplicationCard = ({ app, onClick, isEditing, onDelete, onEdit }: Props) => {
  return (
    <div
      onClick={onClick}
      className="relative bg-white dark:bg-gray-800 rounded-xl shadow-sm hover:shadow-md transition-shadow p-4 flex flex-col items-center cursor-pointer w-32 h-36 border border-gray-200 dark:border-gray-700 group"
    >
      {isEditing && (
        <>
          <button
            onClick={(e) => { e.stopPropagation(); onDelete?.(); }}
            className="absolute -top-2 -right-2 bg-red-500 text-white rounded-full p-1 shadow-md hover:bg-red-600 z-10"
            title="Delete"
          >
            <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
          <button
            onClick={(e) => { e.stopPropagation(); onEdit?.(); }}
            className="absolute -top-2 -left-2 bg-blue-500 text-white rounded-full p-1 shadow-md hover:bg-blue-600 z-10"
            title="Edit"
          >
            <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15.232 5.232l3.536 3.536m-2.036-5.036a2.5 2.5 0 113.536 3.536L6.5 21.036H3v-3.572L16.732 3.732z" />
            </svg>
          </button>
        </>
      )}
      <div className="w-12 h-12 mb-3 flex items-center justify-center">
        {app.iconUrlThumbnail ? (
          <img
            src={app.iconUrlThumbnail}
            alt={app.name}
            className="w-full h-full object-contain"
          />
        ) : (
          <div className="w-full h-full bg-gray-200 dark:bg-gray-700 rounded-full flex items-center justify-center">
            <span className="text-xs text-gray-500">No Icon</span>
          </div>
        )}
      </div>
      <span className="text-sm font-medium text-center text-gray-800 dark:text-gray-200 line-clamp-2 leading-tight break-words w-full">
        {app.name}
      </span>
    </div>
  );
};
