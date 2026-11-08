import "../../styles/folder/FileBlock.css";

import { useState } from "react";

import { ActionButton } from "./ActionButton";
import { File } from "./File";
import { type FileVariant } from "../../types/fileType";
import { downloadFile } from "../../services/fileService";
import { formatBytes, formatDate } from "../../utils/formatters";

import { DeleteFileModal } from "./DeleteFileModal";
import { WriteFileModal } from "./WriteFileModal";
import { FileMetadataModal } from "./FileMetadataModal";

interface FileBlockProps {
  fileId: string;
  fileName: string;
  fileType: FileVariant;
  modifiedDate: Date;
  size: number;
  onFileDeleted?: () => void;
  onFileUpdated?: () => void;
}

export function FileBlock({
  fileId,
  fileName,
  fileType,
  modifiedDate,
  size,
  onFileDeleted,
  onFileUpdated,
}: FileBlockProps) {
  const [showMetadataModal, setShowMetadataModal] = useState(false);
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [showWriteModal, setShowWriteModal] = useState(false);
  const [isDownloading, setIsDownloading] = useState(false);

  const handleDownload = async () => {
    if (isDownloading) return;
    setIsDownloading(true);
    try {
      await downloadFile({ fileId, fileName });
    } catch (err: unknown) {
      console.error("Error al descargar archivo:", err);
    } finally {
      setIsDownloading(false);
    }
  };

  return (
    <div className="file-block">
      <div className="folder-block__file-wrapper">
        <File fileName={fileName} fileType={fileType} />
      </div>

      <p className="file-block__text">{formatDate(modifiedDate)}</p>
      <p className="file-block__text">{formatBytes(size)}</p>

      <div className="file-block-actions">
        <ActionButton
          onClick={() => setShowMetadataModal(true)}
          variant="info"
          title="Ver detalles técnicos del archivo"
        />
        {showMetadataModal ? (
          <FileMetadataModal
            fileId={fileId}
            fallbackFileName={fileName}
            fallbackFileType={fileType}
            closeMethod={() => setShowMetadataModal(false)}
          />
        ) : null}

        <ActionButton
          onClick={() => setShowWriteModal(true)}
          variant="write"
          title="Actualizar archivo"
        />
        {showWriteModal ? (
          <WriteFileModal
            fileId={fileId}
            fileName={fileName}
            closeMethod={() => setShowWriteModal(false)}
            onUpdateSuccess={onFileUpdated}
          />
        ) : null}

        <ActionButton
          onClick={handleDownload}
          variant="download"
          disabled={isDownloading}
          title={isDownloading ? "Descargando..." : "Descargar archivo"}
        />

        <ActionButton
          onClick={() => setShowDeleteModal(true)}
          variant="delete"
          title="Eliminar archivo"
        />
        {showDeleteModal ? (
          <DeleteFileModal
            fileId={fileId}
            fileName={fileName}
            closeMethod={() => setShowDeleteModal(false)}
            onDeleteSuccess={onFileDeleted}
          />
        ) : null}
      </div>
    </div>
  );
}
