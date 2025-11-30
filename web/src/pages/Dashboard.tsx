import { useAuth } from 'react-oidc-context';
import { useMemo, useState } from 'react';
import { useApplications, useCategories, useDeleteApplication, useDeleteCategory } from '../data/hooks';
import { ApplicationCard } from '../components/ApplicationCard';
import { useTheme } from '../hooks/useTheme';
import { useIsAdmin } from '../hooks/useIsAdmin';
import { ApplicationModal } from '../components/ApplicationModal';
import { CategoryModal } from '../components/CategoryModal';
import type { Application, Category } from '../data/models';

export const Dashboard = () => {
  const auth = useAuth();
  const { theme, setTheme } = useTheme();
  const isAdmin = useIsAdmin();
  const [isEditing, setIsEditing] = useState(false);
  const deleteApp = useDeleteApplication();
  const deleteCat = useDeleteCategory();

  const [selectedApp, setSelectedApp] = useState<Application | undefined>(undefined);
  const [isAppModalOpen, setIsAppModalOpen] = useState(false);
  const [targetCategory, setTargetCategory] = useState<Category | undefined>(undefined);

  const [isCatModalOpen, setIsCatModalOpen] = useState(false);
  const [selectedCategory, setSelectedCategory] = useState<Category | undefined>(undefined);

  const { data: categories, isLoading: catsLoading } = useCategories();
  const { data: apps, isLoading: appsLoading } = useApplications();

  const dashboardData = useMemo(() => {
    if (!categories || !apps) return [];

    const sortedCategories = [...categories].sort((a, b) => a.sortOrder - b.sortOrder);

    return sortedCategories.map(cat => {
        const catApps = apps.filter(app =>
            app.applicationCategories.some(ac => ac.category?.id === cat.id)
        ).map(app => {
            const ac = app.applicationCategories.find(link => link.category?.id === cat.id);
            return { ...app, sortOrder: ac?.sortOrder || 0 };
        }).sort((a, b) => a.sortOrder - b.sortOrder);

        return { category: cat, apps: catApps };
    });
  }, [categories, apps]);

  const handleEditApp = (app: Application) => {
    setSelectedApp(app);
    setTargetCategory(undefined);
    setIsAppModalOpen(true);
  };

  const handleAddApp = (category: Category) => {
    setSelectedApp(undefined);
    setTargetCategory(category);
    setIsAppModalOpen(true);
  };

  const handleEditCategory = (category: Category) => {
    setSelectedCategory(category);
    setIsCatModalOpen(true);
  };

  const handleAddCategory = () => {
    setSelectedCategory(undefined);
    setIsCatModalOpen(true);
  };

  if (auth.isLoading) return <div className="flex justify-center items-center min-h-screen bg-gray-50 dark:bg-gray-900 dark:text-white">Loading auth...</div>;

  if (!auth.isAuthenticated) {
     return (
       <div className="flex flex-col items-center justify-center min-h-screen bg-gray-100 dark:bg-gray-900 text-gray-900 dark:text-white transition-colors">
         <h1 className="text-4xl font-bold mb-8">PortURL</h1>
         <button onClick={() => auth.signinRedirect()} className="bg-blue-600 text-white px-6 py-3 rounded-lg hover:bg-blue-700 transition-colors shadow-lg">Login with SSO</button>
       </div>
     )
  }

  if (catsLoading || appsLoading) return <div className="flex justify-center items-center min-h-screen bg-gray-50 dark:bg-gray-900 dark:text-white">Loading dashboard...</div>;

  return (
    <div className="min-h-screen bg-gray-50 dark:bg-gray-900 transition-colors">
        <nav className="bg-white dark:bg-gray-800 shadow px-6 py-4 flex justify-between items-center sticky top-0 z-10">
            <h1 className="text-xl font-bold text-gray-800 dark:text-white">PortURL</h1>
            <div className="flex items-center gap-4">
                 {isAdmin && (
                    <button
                        onClick={() => setIsEditing(!isEditing)}
                        className={`px-3 py-1 rounded text-sm font-medium transition-colors ${isEditing ? 'bg-blue-100 text-blue-700 dark:bg-blue-900 dark:text-blue-200' : 'text-gray-600 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700'}`}
                    >
                        {isEditing ? 'Done' : 'Edit'}
                    </button>
                 )}
                 <button
                    onClick={() => setTheme(theme === 'dark' ? 'light' : 'dark')}
                    className="p-2 rounded-full hover:bg-gray-100 dark:hover:bg-gray-700 text-gray-600 dark:text-gray-300 transition-colors"
                    title="Toggle Theme"
                 >
                    {theme === 'dark' ? '☀️' : '🌙'}
                 </button>
                 <div className="hidden md:flex flex-col items-end">
                    <span className="text-sm font-medium text-gray-800 dark:text-gray-200">{auth.user?.profile.preferred_username}</span>
                    <span className="text-xs text-gray-500 dark:text-gray-400">{auth.user?.profile.email}</span>
                 </div>
                 <button onClick={() => auth.removeUser()} className="text-red-600 hover:text-red-800 text-sm font-medium ml-2">Logout</button>
            </div>
        </nav>

        <main className="p-6 max-w-7xl mx-auto">
            {dashboardData.map(({ category, apps }) => (
                <div key={category.id} className="mb-8">
                    <div className="flex items-center justify-between mb-4 border-b border-gray-200 dark:border-gray-700 pb-2">
                        <div className="flex items-center gap-2">
                            <h2 className="text-xl font-semibold text-gray-700 dark:text-gray-200">{category.name}</h2>
                            {isEditing && (
                                <button onClick={() => handleEditCategory(category)} className="text-blue-500 hover:text-blue-600 text-sm">
                                    (Edit)
                                </button>
                            )}
                        </div>
                        {isEditing && (
                             <button
                                onClick={() => { if(confirm(`Delete category "${category.name}"?`)) deleteCat.mutate(category.id); }}
                                className="text-sm text-red-500 hover:underline"
                             >
                                Delete Category
                             </button>
                        )}
                    </div>
                    <div className="flex flex-wrap gap-6">
                        {apps.map(app => (
                            <ApplicationCard
                                key={app.id}
                                app={app}
                                onClick={() => window.open(app.url, '_blank')}
                                isEditing={isEditing}
                                onDelete={() => {
                                    if (confirm(`Delete ${app.name}?`)) {
                                        deleteApp.mutate(app.id!);
                                    }
                                }}
                                onEdit={() => handleEditApp(app)}
                            />
                        ))}
                        {apps.length === 0 && <p className="text-gray-400 italic text-sm py-4">No applications in this category.</p>}
                        {isEditing && (
                            <button
                                onClick={() => handleAddApp(category)}
                                className="w-32 h-36 border-2 border-dashed border-gray-300 dark:border-gray-600 rounded-xl flex items-center justify-center text-gray-400 hover:border-gray-400 hover:text-gray-500 transition-colors"
                            >
                                <span className="text-2xl">+</span>
                            </button>
                        )}
                    </div>
                </div>
            ))}
            {dashboardData.length === 0 && (
                 <div className="text-center py-20 text-gray-500 dark:text-gray-400">
                    <p className="text-lg">No categories found.</p>
                 </div>
            )}
            {isEditing && (
                <div className="flex justify-center mt-8">
                    <button onClick={handleAddCategory} className="bg-gray-200 dark:bg-gray-700 text-gray-800 dark:text-white px-4 py-2 rounded-md hover:bg-gray-300 dark:hover:bg-gray-600">
                        + Add New Category
                    </button>
                </div>
            )}
        </main>

        <ApplicationModal
            isOpen={isAppModalOpen}
            onClose={() => setIsAppModalOpen(false)}
            app={selectedApp}
            initialCategory={targetCategory}
        />
        <CategoryModal
            isOpen={isCatModalOpen}
            onClose={() => setIsCatModalOpen(false)}
            category={selectedCategory}
        />
    </div>
  );
};
