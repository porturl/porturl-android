import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useAuth } from 'react-oidc-context';
import axios from 'axios';
import type { Application, Category } from './models';

const BASE_URL = import.meta.env.VITE_BACKEND_URL || 'http://localhost:8080';

// Hook to get an axios instance with the current token
const useApiClient = () => {
  const auth = useAuth();
  const token = auth.user?.access_token;

  return axios.create({
    baseURL: BASE_URL,
    headers: {
      Authorization: token ? `Bearer ${token}` : '',
    },
  });
};

export const useApplications = () => {
  const client = useApiClient();
  const auth = useAuth();
  return useQuery({
    queryKey: ['applications'],
    queryFn: async () => {
      const { data } = await client.get<Application[]>('/api/applications');
      return data;
    },
    enabled: !!auth.user,
  });
};

export const useCategories = () => {
  const client = useApiClient();
  const auth = useAuth();
  return useQuery({
    queryKey: ['categories'],
    queryFn: async () => {
      const { data } = await client.get<Category[]>('/api/categories');
      return data;
    },
    enabled: !!auth.user,
  });
};

export const useCreateApplication = () => {
  const client = useApiClient();
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (app: Application) => {
      const { data } = await client.post<Application>('/api/applications', app);
      return data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['applications'] });
    },
  });
};

export const useUpdateApplication = () => {
  const client = useApiClient();
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (app: Application) => {
      const { data } = await client.put<Application>(`/api/applications/${app.id}`, app);
      return data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['applications'] });
    },
  });
};

export const useDeleteApplication = () => {
  const client = useApiClient();
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (id: number) => {
      await client.delete(`/api/applications/${id}`);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['applications'] });
    },
  });
};

export const useCreateCategory = () => {
  const client = useApiClient();
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (category: Category) => {
      const { data } = await client.post<Category>('/api/categories', category);
      return data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['categories'] });
    },
  });
};

export const useUpdateCategory = () => {
  const client = useApiClient();
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (category: Category) => {
      const { data } = await client.put<Category>(`/api/categories/${category.id}`, category);
      return data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['categories'] });
    },
  });
};

export const useDeleteCategory = () => {
  const client = useApiClient();
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (id: number) => {
      await client.delete(`/api/categories/${id}`);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['categories'] });
    },
  });
};

export const useReorderApplications = () => {
  const client = useApiClient();
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (apps: Application[]) => {
      await client.post('/api/applications/reorder', apps);
    },
    onSuccess: () => {
       // Ideally we don't invalidate immediately to prevent UI jumping if we already updated optimistically
       // But for simplicity:
       queryClient.invalidateQueries({ queryKey: ['applications'] });
    }
  });
};

export const useReorderCategories = () => {
    const client = useApiClient();
    const queryClient = useQueryClient();
    return useMutation({
      mutationFn: async (categories: Category[]) => {
        await client.post('/api/categories/reorder', categories);
      },
      onSuccess: () => {
         queryClient.invalidateQueries({ queryKey: ['categories'] });
      }
    });
  };
