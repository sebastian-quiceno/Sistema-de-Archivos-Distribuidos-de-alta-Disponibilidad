import { useCallback, useEffect, useMemo, useRef, useState } from "react";

import "../../styles/folder/FileExplorer.css";

import { getFiles, getSubfolders } from "../../services/folderService";

import { FolderBlock } from "./FolderBlock";
import { FileBlock } from "./FileBlock";
import { Breadcrumb, type BreadcrumbItem } from "./Breadcrumb";

import { type FolderType } from "../../types/folderType";
import { type MetadataFileType } from "../../types/fileType";
import { useAuth } from "../../hooks/users/useAuth";

interface FileExplorerProps {
  refreshToken?: number;
  navigateUpToken?: number;
  searchQuery?: string;
  onCurrentFolderChange?: (folderId: string) => void;
}

export function FileExplorer({
  refreshToken = 0,
  navigateUpToken = 0,
  searchQuery = "",
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
  const [breadcrumbs, setBreadcrumbs] = useState<BreadcrumbItem[]>([]);

  const refreshContent = useCallback(async () => {
    if (!folderId) return;
    try {
      const [subfolders, filesResponse] = await Promise.all([
        getSubfolders(folderId),
        getFiles(folderId),
      ]);
      setFolders(subfolders);
      setFiles(filesResponse);
    } catch (err: unknown) {
      console.error("Error al refrescar contenido:", err);
    }
  }, [folderId]);

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
          parentByFolderId.current[folderId] =
            folderId === rootFolderId
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
      setSelectedFolderId(
        parentFolderId === rootFolderId ? null : parentFolderId,
      );
      setBreadcrumbs((prev) => {
        if (prev.length <= 1) {
          return rootFolderId ? [{ id: rootFolderId, name: "Mi Unidad" }] : [];
        }
        return prev.slice(0, -1);
      });
    }
  }, [folderId, navigateUpToken, rootFolderId]);

  function openFolder(nextFolderId: string, nextFolderName: string) {
    if (!nextFolderId || nextFolderId === folderId) {
      return;
    }
    setSelectedFolderId(nextFolderId);
    setBreadcrumbs((prev) => {
      const base =
        prev.length > 0
          ? prev
          : rootFolderId
            ? [{ id: rootFolderId, name: "Mi Unidad" }]
            : [];
      const existingIndex = base.findIndex((item) => item.id === nextFolderId);
      if (existingIndex !== -1) {
        return base.slice(0, existingIndex + 1);
      }
      return [...base, { id: nextFolderId, name: nextFolderName }];
    });
  }

  function handleBreadcrumbNavigate(targetFolderId: string) {
    if (targetFolderId === folderId) {
      return;
    }

    if (targetFolderId === rootFolderId) {
      setSelectedFolderId(null);
      setBreadcrumbs(
        rootFolderId ? [{ id: rootFolderId, name: "Mi Unidad" }] : [],
      );
      return;
    }

    setSelectedFolderId(targetFolderId);
    setBreadcrumbs((prev) => {
      const base =
        prev.length > 0
          ? prev
          : rootFolderId
            ? [{ id: rootFolderId, name: "Mi Unidad" }]
            : [];
      const targetIndex = base.findIndex((item) => item.id === targetFolderId);
      if (targetIndex !== -1) {
        return base.slice(0, targetIndex + 1);
      }
      return base;
    });
  }

  const normalizedQuery = (searchQuery || "").trim().toLowerCase();

  const filteredFolders = useMemo(() => {
    if (!normalizedQuery) return folders;
    return folders.filter((folder) =>
      folder.name.toLowerCase().includes(normalizedQuery),
    );
  }, [folders, normalizedQuery]);

  const filteredFiles = useMemo(() => {
    if (!normalizedQuery) return files;
    return files.filter((file) =>
      file.fileName.toLowerCase().includes(normalizedQuery),
    );
  }, [files, normalizedQuery]);

  const isSearching = normalizedQuery.length > 0;
  const hasNoSearchResults =
    isSearching && filteredFolders.length === 0 && filteredFiles.length === 0;
  const isEmptyFolder =
    !isSearching && folders.length === 0 && files.length === 0;

  const activeBreadcrumbs =
    breadcrumbs.length > 0
      ? breadcrumbs
      : rootFolderId
        ? [{ id: rootFolderId, name: "Mi Unidad" }]
        : [];

  return (
    <div className="file-explorer">
      <Breadcrumb
        items={activeBreadcrumbs}
        onNavigate={handleBreadcrumbNavigate}
      />
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
        {filteredFolders.map((item: FolderType) => (
          <FolderBlock
            key={item.folderId}
            folderId={item.folderId}
            folderName={item.name}
            modifiedDate={new Date(item.createdDate)}
            onOpen={openFolder}
            onFolderDeleted={(id) => {
              setFolders((prev) => prev.filter((f) => f.folderId !== id));
              refreshContent();
            }}
            onFolderRenamed={(newName) => {
              setBreadcrumbs((prev) =>
                prev.map((crumb) =>
                  crumb.id === item.folderId
                    ? { ...crumb, name: newName }
                    : crumb,
                ),
              );
              refreshContent();
            }}
          />
        ))}

        {filteredFiles.map((item: MetadataFileType) => (
          <FileBlock
            key={item.id}
            fileId={item.id}
            fileName={item.fileName}
            fileType={item.fileTypeName}
            modifiedDate={new Date(item.lastTimeModified)}
            size={item.bytesSize}
            onFileDeleted={() => {
              setFiles((prev) => prev.filter((f) => f.id !== item.id));
              refreshContent();
            }}
            onFileUpdated={() => {
              refreshContent();
            }}
          />
        ))}

        {hasNoSearchResults && (
          <div className="file-explorer__empty-state">
            <p>No se encontraron resultados para "{searchQuery}"</p>
          </div>
        )}

        {isEmptyFolder && (
          <div className="file-explorer__empty-state">
            <p>Esta carpeta está vacía.</p>
          </div>
        )}
      </div>
    </div>
  );
}
