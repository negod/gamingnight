import { apiRequest } from '../../../shared/api/apiClient';
import type { Item, ItemFormValues } from '../../../shared/types/item';

export function listItems(): Promise<Item[]> {
  return apiRequest<Item[]>('/items');
}

export function getItem(id: string): Promise<Item> {
  return apiRequest<Item>(`/items/${id}`);
}

export function createItem(values: ItemFormValues): Promise<Item> {
  return apiRequest<Item>('/items', {
    method: 'POST',
    body: JSON.stringify(values),
  });
}

export function updateItem(id: string, values: ItemFormValues): Promise<Item> {
  return apiRequest<Item>(`/items/${id}`, {
    method: 'PUT',
    body: JSON.stringify(values),
  });
}

export function deleteItem(id: string): Promise<void> {
  return apiRequest<void>(`/items/${id}`, {
    method: 'DELETE',
  });
}
