import { useState } from "react";
import "../../styles/folder/FolderBlock.css";

import { ActionButton } from "./ActionButton";
import { File } from "./File";

import { DeleteModal } from "../../components/folders/DeleteModal";
import { WriteModal } from "../../components/folders/WriteModal";
import { formatDate } from "../../utils/formatters";

interface FolderBlockProps {
  folderId: string;
  folderName: string;
  modifiedDate: Date;
  onOpen?: (folderId: string) => void;
  onFolderDeleted?: (folderId: string) => void;
  onFolderRenamed?: (newFolderName: string) => void;
}

export function FolderBlock({
  folderId,
  folderName,
  modifiedDate,
  onOpen,
  onFolderDeleted,
  onFolderRenamed,
}: FolderBlockProps) {
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [showWriteModal, setShowWriteModal] = useState(false);

  return (
    <div className="folder-block">
      <button
        className="folder-block__file-wrapper"
        type="button"
        onClick={() => onOpen?.(folderId)}
      >
        <File fileName={folderName} fileType="folder" />
      </button>
      <p className="folder-block__text">{formatDate(modifiedDate)}</p>
      <p className="folder-block__text">. . .</p>

      <div className="folder-block-actions">
        <ActionButton
          onClick={() => setShowWriteModal(true)}
          variant="write"
          title="Renombrar carpeta"
        />
        {showWriteModal ? (
          <WriteModal
            folderId={folderId}
            folderName={folderName}
            closeMethod={() => setShowWriteModal(false)}
            onRenameSuccess={onFolderRenamed}
          />
        ) : null}

        <ActionButton
          onClick={() => setShowDeleteModal(true)}
          variant="delete"
          title="Eliminar carpeta"
        />
        {showDeleteModal ? (
          <DeleteModal
            folderId={folderId}
            folderName={folderName}
            closeMethod={() => setShowDeleteModal(false)}
            onDeleteSuccess={onFolderDeleted}
          />
        ) : null}
      </div>
    </div>
  );
}
