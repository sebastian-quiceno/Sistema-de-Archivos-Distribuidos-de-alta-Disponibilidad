import { useCallback, useEffect, useState } from "react";

import "../../styles/folder/FileExplorer.css";

import { getFiles, getSubfolders } from "../../services/folderService";

import { FolderBlock } from "./FolderBlock";
import { FileBlock } from "./FileBlock";

import { type FolderType } from "../../types/folderType";
import { type MetadataFileType } from "../../types/fileType";



interface FileExplorerProps {
  folderId: string;
}

export function FileExplorer({ folderId }: FileExplorerProps) {
  const [folders, setFolders] = useState<FolderType[]>([]);
  const [files, setFiles] = useState<MetadataFileType[]>();
  const [error, setError] = useState("");


  useEffect(() => {
    const fetchFolders = async () => {
      try {
        const response = await getSubfolders(folderId);
        // Mapea la respuesta del backend al contexto
        setFolders(response);
      } catch (error) {
        setError(
          error instanceof Error ? error.message : "No se pudo iniciar sesión",
        );
      }
    };

    const fetchFiles = async () => {
      try {
        const response = await getFiles(folderId);
        // Mapea la respuesta del backend al contexto
        setFiles(response);
      } catch (error) {
        setError(
          error instanceof Error ? error.message : "No se pudo iniciar sesión",
        );
      }
    };

    fetchFolders();
    fetchFiles();
  }, [folderId]);

  return (
    <div className="file-explorer">
      <h2 className="file-explorer__title">Mi Unidad</h2>
      <div className="file-explorer__files">
        <div className="file-explorer__header">
          <p className="file-explorer__header__text">Nombre</p>
          <p>Fecha Modificación</p>
          <p>Tamaño</p>
          <p>Acciones</p>
        </div>
        <hr />
        {folders.map((item: FolderType) => (
          <FolderBlock folderId={item.folderId} folderName={item.name} modifiedDate={new Date(item.createdDate)}/>
        ))}
      </div>
    </div>
  );
}
