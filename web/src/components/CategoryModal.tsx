import { useState, useEffect } from 'react';
import type { Category } from '../data/models';
import { useCreateCategory, useUpdateCategory } from '../data/hooks';
import { Modal } from './Modal';

interface Props {
  isOpen: boolean;
  onClose: () => void;
  category?: Category;
}

export const CategoryModal = ({ isOpen, onClose, category }: Props) => {
    const create = useCreateCategory();
    const update = useUpdateCategory();

    const [name, setName] = useState('');

    useEffect(() => {
        if (isOpen) {
            // eslint-disable-next-line react-hooks/set-state-in-effect
            setName(category?.name || '');
        } else {
            setName('');
        }
    }, [isOpen, category]);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        const payload: any = {
            ...(category || {}),
            name,
            sortOrder: category?.sortOrder || 999,
            applicationSortMode: category?.applicationSortMode || 'CUSTOM',
            enabled: true
        };

        if (category?.id) {
            await update.mutateAsync(payload);
        } else {
            await create.mutateAsync(payload);
        }
        onClose();
    };

    return (
        <Modal isOpen={isOpen} onClose={onClose} title={category ? 'Edit Category' : 'Add Category'}>
            <form onSubmit={handleSubmit} className="space-y-4">
                 <div>
                    <label className="block text-sm font-medium text-gray-700 dark:text-gray-300">Name</label>
                    <input className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:text-white p-2 border" value={name} onChange={e => setName(e.target.value)} required />
                </div>
                <div className="flex justify-end gap-2 mt-4">
                    <button type="button" onClick={onClose} className="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50">Cancel</button>
                    <button type="submit" className="px-4 py-2 text-sm font-medium text-white bg-blue-600 rounded-md hover:bg-blue-700">Save</button>
                </div>
            </form>
        </Modal>
    )
}
