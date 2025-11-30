import { useState, useEffect } from 'react';
import type { Application, Category } from '../data/models';
import { useCreateApplication, useUpdateApplication } from '../data/hooks';
import { Modal } from './Modal';

interface Props {
  isOpen: boolean;
  onClose: () => void;
  app?: Application;
  initialCategory?: Category;
}

export const ApplicationModal = ({ isOpen, onClose, app, initialCategory }: Props) => {
    const create = useCreateApplication();
    const update = useUpdateApplication();

    const [name, setName] = useState('');
    const [url, setUrl] = useState('');
    const [description, setDescription] = useState('');

    useEffect(() => {
        if (isOpen) {
            if (app) {
                // eslint-disable-next-line react-hooks/set-state-in-effect
                setName(app.name);
                setUrl(app.url);
                setDescription(app.description || '');
            } else {
                setName('');
                setUrl('');
                setDescription('');
            }
        }
    }, [isOpen, app]);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        const payload: Application = {
            ...(app || {}),
            name,
            url,
            description,
            // If editing, preserve existing categories. If new, add to initial category if provided.
            applicationCategories: app?.applicationCategories || (initialCategory ? [{ category: initialCategory, sortOrder: 999 }] : []),
        };

        if (app?.id) {
            await update.mutateAsync({ ...payload, id: app.id });
        } else {
            await create.mutateAsync(payload);
        }
        onClose();
    };

    return (
        <Modal isOpen={isOpen} onClose={onClose} title={app ? 'Edit Application' : 'Add Application'}>
            <form onSubmit={handleSubmit} className="space-y-4">
                <div>
                    <label className="block text-sm font-medium text-gray-700 dark:text-gray-300">Name</label>
                    <input className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:text-white p-2 border" value={name} onChange={e => setName(e.target.value)} required />
                </div>
                <div>
                    <label className="block text-sm font-medium text-gray-700 dark:text-gray-300">URL</label>
                    <input className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:text-white p-2 border" value={url} onChange={e => setUrl(e.target.value)} required />
                </div>
                <div>
                    <label className="block text-sm font-medium text-gray-700 dark:text-gray-300">Description</label>
                    <textarea className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:text-white p-2 border" value={description} onChange={e => setDescription(e.target.value)} />
                </div>
                <div className="flex justify-end gap-2 mt-4">
                    <button type="button" onClick={onClose} className="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50">Cancel</button>
                    <button type="submit" className="px-4 py-2 text-sm font-medium text-white bg-blue-600 rounded-md hover:bg-blue-700">Save</button>
                </div>
            </form>
        </Modal>
    );
};
