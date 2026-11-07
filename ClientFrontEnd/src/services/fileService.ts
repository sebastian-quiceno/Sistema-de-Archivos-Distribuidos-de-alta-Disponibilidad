import { apiClient, getApiErrorMessage } from "../apis/apiClient";

import {
  type DownloadFileType,
  type IntegrityCheckReportType,
  type MetadataFileType,
  type UpdateFileRequestType,
  type UploadFileRequestType,
} from "../types/fileType";


export const uploadFile = async ({ file, folderId, fileTypeName, fileName }: UploadFileRequestType): Promise<void> => {

  const request = new FormData();

  request.append('file', file)
  request.append('folderId', folderId)
  request.append("fileTypeName", fileTypeName);

  if (fileName) {
    request.append('name', fileName);
  }

  try {
    await apiClient.post<void>(
      "/files/upload",
      request
    );

  } catch (error: unknown) {
    throw new Error(getApiErrorMessage(error, "Error al subir archivo"), { cause: error });
  }
};

export async function getFileMetadata(fileId: string): Promise<MetadataFileType> {
  if (!fileId?.trim()) {
    throw new Error("El identificador del archivo es obligatorio");
  }

  try {
    const response = await apiClient.get<MetadataFileType>(
      `/files/${encodeURIComponent(fileId)}`,
    );
    return response.data;
  } catch (error: unknown) {
    throw new Error(getApiErrorMessage(error, "Error al consultar los metadatos del archivo"), { cause: error });
  }
}

export async function updateFile({ fileId, file }: UpdateFileRequestType): Promise<void> {
  if (!fileId?.trim()) {
    throw new Error("El identificador del archivo es obligatorio");
  }
  if (!file || file.size === 0) {
    throw new Error("El archivo es obligatorio y no puede estar vacío");
  }

  const request = new FormData();
  request.append("file", file);

  try {
    await apiClient.put<void>(
      `/files/${encodeURIComponent(fileId)}`,
      request,
    );
  } catch (error: unknown) {
    throw new Error(getApiErrorMessage(error, "Error al actualizar archivo"), { cause: error });
  }
}

export async function deleteFile(fileId: string): Promise<void> {
  if (!fileId?.trim()) {
    throw new Error("El identificador del archivo es obligatorio");
  }

  try {
    await apiClient.delete<void>(`/files/${encodeURIComponent(fileId)}`);
  } catch (error: unknown) {
    throw new Error(getApiErrorMessage(error, "Error al eliminar archivo"), { cause: error });
  }
}

export async function checkFileIntegrity(pageSize?: number): Promise<IntegrityCheckReportType> {
  try {
    const response = await apiClient.post<IntegrityCheckReportType>(
      "/files/check-integrity",
      undefined,
      pageSize === undefined ? undefined : { params: { pageSize } },
    );
    return response.data;
  } catch (error: unknown) {
    throw new Error(getApiErrorMessage(error, "Error al comprobar la integridad de los archivos"), { cause: error });
  }
}

export const downloadFile = async ({ fileId }: DownloadFileType): Promise<void> => {
  if (!fileId?.trim()) {
    throw new Error("El identificador del archivo es obligatorio");
  }

  try {
    const response = await apiClient.get<Blob>(
      `/files/${encodeURIComponent(fileId)}/download`,
      { responseType: "blob" },
    );

    if (!(response.data instanceof Blob) || response.data.size === 0) {
      throw new Error("El archivo descargado está vacío");
    }

    const contentDisposition = response.headers["content-disposition"];
    const encodedFileName = contentDisposition?.match(/filename\*=UTF-8''([^;]+)/i)?.[1];
    const fileName = encodedFileName
      ? decodeURIComponent(encodedFileName)
      : contentDisposition?.match(/filename="?([^";]+)"?/i)?.[1];
    const downloadName = fileName?.trim() || `archivo-${fileId}`;
    const downloadUrl = URL.createObjectURL(response.data);
    const link = document.createElement("a");

    link.href = downloadUrl;
    link.download = downloadName;
    document.body.appendChild(link);
    link.click();
    link.remove();
    URL.revokeObjectURL(downloadUrl);
  } catch (error: unknown) {
    throw new Error(getApiErrorMessage(error, "Error al descargar archivo"), { cause: error });
  }
};
