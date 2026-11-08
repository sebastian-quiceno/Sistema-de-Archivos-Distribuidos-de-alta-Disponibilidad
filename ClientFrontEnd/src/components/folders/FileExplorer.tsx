import { useEffect, useRef, useState } from "react";

import "../../styles/folder/FileExplorer.css";

import { getFiles, getSubfolders } from "../../services/folderService";

import { FolderBlock } from "./FolderBlock";
import { FileBlock } from "./FileBlock";

import { type FolderType } from "../../types/folderType";
import { type MetadataFileType } from "../../types/fileType";
import { useAuth } from "../../hooks/users/useAuth";

interface FileExplorerProps {
  refreshToken?: number;
  navigateUpToken?: number;
  onCurrentFolderChange?: (folderId: string) => void;
}

export function FileExplorer({
  refreshToken = 0,
  navigateUpToken = 0,
  onCurrentFolderChange,
}: FileExplorerProps) {
  const { auth } = useAuth();
  const rootFolderId = auth?.rootFolderId?.trim() || null;
  const [selectedFolderId, setSelectedFolderId] = useState<string | null>(null);
  const folderId = selectedFolderId ?? rootFolderId;
  const parentByFolderId = useRef<Record<string, string | null>>({});
  const [folders, setFolders] = useState<FolderType[]>([]);
  const [files, setFiles] = useState<MetadataFileType[]>([]);
  const [error, setError] = useState("");

  useEffect(() => {
    if (!folderId) {
      return;
    }

    let active = true;

    const fetchContent = async () => {
      setError("");
      try {
        const [subfolders, filesResponse] = await Promise.all([
          getSubfolders(folderId),
          getFiles(folderId),
        ]);
        if (active) {
          subfolders.forEach((folder) => {
            parentByFolderId.current[folder.folderId] = folder.fatherFolderId;
          });
          parentByFolderId.current[folderId] = folderId === rootFolderId
            ? null
            : parentByFolderId.current[folderId] ?? rootFolderId;
          setFolders(subfolders);
          setFiles(filesResponse);
          onCurrentFolderChange?.(folderId);
        }
      } catch (error) {
        if (active) {
          setError(
            error instanceof Error
              ? error.message
              : "No se pudo consultar la carpeta",
          );
        }
      }
    };

    fetchContent();
    return () => {
      active = false;
    };
  }, [folderId, onCurrentFolderChange, refreshToken, rootFolderId]);

  useEffect(() => {
    if (navigateUpToken === 0 || !folderId || folderId === rootFolderId) {
      return;
    }

    const parentFolderId = parentByFolderId.current[folderId];
    if (parentFolderId && parentFolderId !== folderId) {
      setSelectedFolderId(parentFolderId === rootFolderId ? null : parentFolderId);
    }
  }, [folderId, navigateUpToken, rootFolderId]);

  function openFolder(nextFolderId: string) {
    if (!nextFolderId || nextFolderId === folderId) {
      return;
    }
    setSelectedFolderId(nextFolderId);
  }

  return (
    <div className="file-explorer">
      <h2 className="file-explorer__title">
        {folderId === rootFolderId ? "Mi Unidad" : "Carpeta"}
      </h2>
      {!auth && <p>Inicia sesión para consultar tus archivos.</p>}
      {auth && !folderId && <p>No se encontró la carpeta raíz del usuario.</p>}
      {error && <p>{error}</p>}
      <div className="file-explorer__files">
        <div className="file-explorer__header">
          <p>Nombre</p>
          <p>Fecha Modificación</p>
          <p>Tamaño</p>
          <p>Acciones</p>
        </div>
        <hr />
        {folders.map((item: FolderType) => (
          <FolderBlock key={item.folderId} folderId={item.folderId} folderName={item.name} modifiedDate={new Date(item.createdDate)} onOpen={openFolder}/>
        ))}
        
        {files.map((item: MetadataFileType) => (
          <FileBlock key={item.id} fileId={item.id} fileName={item.fileName} fileType={item.fileTypeName} modifiedDate={new Date(item.lastTimeModified)} size={item.bytesSize} />
        ))}
      </div>
    </div>
  );
}
