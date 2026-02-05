export interface Category {
  id: number;
  name: string;
  sortOrder: number;
  applicationSortMode: 'CUSTOM' | 'ALPHABETICAL';
  icon?: string;
  description?: string;
  enabled: boolean;
}

export interface ApplicationCategory {
  category?: Category;
  sortOrder: number;
}

export interface Application {
  id?: number;
  name: string;
  url: string;
  description?: string;
  applicationCategories: ApplicationCategory[];
  iconLarge?: string;
  iconMedium?: string;
  iconThumbnail?: string;
  iconUrlLarge?: string;
  iconUrlMedium?: string;
  iconUrlThumbnail?: string;
}

export interface ImageUploadResponse {
    fileName: string;
}
