export interface CreateFolderRequestType {
    folderName: string;
    fatherId?: string | null;
}

export interface MoveFolderRequestType {
    folderId: string;
    newFatherFolderId?: string | null;
}

export interface RenameFolderRequestType {
    folderId: string;
    newFolderName: string | null;
}

export interface FolderType {
    folderId: string;
    name: string;
    createdDate: string;
}
