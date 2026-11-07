export interface UploadFileRequestType{
    file: File;
    folderId: string;
    fileTypeName: string;
    fileName?: string;
} 

export interface DownloadFileType{
    fileId: string
}

export interface UpdateFileRequestType {
    fileId: string;
    file: File;
}

export interface MetadataFileType {
    id: string;
    folderId: string;
    fileTypeName: string;
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