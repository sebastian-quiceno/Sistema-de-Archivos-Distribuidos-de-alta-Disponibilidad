import { apiClient, getApiErrorMessage } from "../apis/apiClient";
import {
  type CreateFolderRequestType,
  type FolderType,
  type MoveFolderRequestType,
  type RenameFolderRequestType,
} from "../types/folderType";

import {type MetadataFileType} from '../types/fileType'

export async function createFolder({ folderName, fatherId }: CreateFolderRequestType): Promise<void> {
  if (!folderName?.trim()) {
    throw new Error("El nombre de la carpeta es obligatorio");
  }

  try {
    await apiClient.post<void>("/folders", {
      folderName: folderName.trim(),
      fatherId: fatherId || null,
    });
  } catch (error: unknown) {
    throw new Error(getApiErrorMessage(error, "Error al crear carpeta"), { cause: error });
  }
}

export async function getSubfolders(folderId: string): Promise<FolderType[]> {
  if (!folderId?.trim()) {
    throw new Error("El identificador de la carpeta es obligatorio");
  }

  try {
    const response = await apiClient.get<FolderType[]>(
      `/folders/${encodeURIComponent(folderId)}/subfolders`,
    );
    return response.data;
  } catch (error: unknown) {
    throw new Error(getApiErrorMessage(error, "Error al consultar subcarpetas"), { cause: error });
  }
}

export async function getFiles(folderId: string | null): Promise<MetadataFileType[]> {
  try {
    const response = await apiClient.get<MetadataFileType[]>('/folders/subfolders', {
      params: { folderId }
    });
    return response.data;
  } catch (error: unknown) {
    throw new Error(getApiErrorMessage(error, "Error al consultar subcarpetas"), { cause: error });
  }
}

export async function moveFolder({ folderId, newFatherFolderId }: MoveFolderRequestType): Promise<void> {
  if (!folderId?.trim()) {
    throw new Error("El identificador de la carpeta es obligatorio");
  }

  try {
    await apiClient.patch<void>(
      `/folders/${encodeURIComponent(folderId)}/move`,
      { newFatherFolderId: newFatherFolderId || null },
    );
  } catch (error: unknown) {
    throw new Error(getApiErrorMessage(error, "Error al mover carpeta"), { cause: error });
  }
}

export async function renameFolder({ folderId, newFolderName }: RenameFolderRequestType): Promise<void> {
  if (!folderId?.trim()) {
    throw new Error("El identificador de la carpeta es obligatorio");
  }

  if (!newFolderName?.trim()) {
    throw new Error("El nuevo nombre de la carpeta es obligatorio");
  }

  try {
    await apiClient.patch<void>(
      `/folders/${encodeURIComponent(folderId)}/rename`,
      { newFolderName: newFolderName || null },
    );
  } catch (error: unknown) {
    throw new Error(getApiErrorMessage(error, "Error al mover renombrar la carpeta"), { cause: error });
  }
}

export async function deleteFolder(folderId: string): Promise<void> {
  if (!folderId?.trim()) {
    throw new Error("El identificador de la carpeta es obligatorio");
  }

  try {
    await apiClient.delete<void>(`/folders/${encodeURIComponent(folderId)}`);
  } catch (error: unknown) {
    throw new Error(getApiErrorMessage(error, "Error al eliminar carpeta"), { cause: error });
  }
}
