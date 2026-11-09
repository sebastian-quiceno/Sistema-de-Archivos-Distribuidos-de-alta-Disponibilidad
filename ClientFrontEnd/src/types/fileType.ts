export interface UploadFileRequestType {
    file: File;
    folderId: string;
    fileTypeName: string;
    fileName?: string;
    onProgress?: (percent: number) => void;
} 

export interface DownloadFileType {
  fileId: string;
  fileName?: string;
}

export interface UpdateFileRequestType {
    fileId: string;
    file: File;
}

export interface RenameFileRequestType {
    fileId: string;
    newFileName: string;
}

export interface MetadataFileType {
    id: string;
    folderId: string;
    fileTypeName: FileVariant;
    fileName: string;
    bytesSize: number;
    totalPartitions: number;
    lastTimeModified: string;
}

export interface FileAuditIssueType {
    fileId: string;
    fileName: string;
    errorMessage: string;
}

export interface IntegrityCheckReportType {
    totalFilesChecked: number;
    healthyFilesCount: number;
    healedFilesCount: number;
    corruptedFilesCount: number;
    issues: FileAuditIssueType[];
}

export type FileVariant =
  | "noType"
  | "folder"
  | "doc"
  | "pdf"
  | "png"
  | "jpg"
  | "mp3"
  | "mp4";