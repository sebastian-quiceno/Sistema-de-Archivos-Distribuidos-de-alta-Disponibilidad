import { useState } from "react";

import { ActionButton } from "./ActionButton";
import { File, type FileVariant } from "./File";
import {
  deleteFolder,
  getSubfolders,
  moveFolder,
  renameFolder,
} from "../../services/folderService";

import { DeleteModal } from "../../components/folders/DeleteModal";
import { WriteModal } from "../../components/folders/WriteModal";

interface FolderBlockProps {
  folderId: string;
  folderType: FileVariant;
  modifiedDate: Date;
  size: number;
}

export function FileBlock({
  folderId,
  folderName,
  folderType,
  modifiedDate,
  size,
}: FolderBlockProps) {
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [showWriteModal, setShowWriteModal] = useState(false);

  return (
    <div className="file-block">
      <File fileName={folderName} fileType="folder" />
      <p>{modifiedDate.getDate()}</p>
      <p>. . .</p>

      <div className="file-block-actions">
        <ActionButton onClick={() => setShowWriteModal(true)} variant="write" />
        {showWriteModal ? (
          <WriteModal
            folderId={folderId}
            folderName={folderName}
            closeMethod={() => setShowWriteModal(false)}
          />
        ) : null}

        <ActionButton onClick={() => null} variant="download" disabled />

        <ActionButton
          onClick={() => setShowDeleteModal(true)}
          variant="delete"
        />
        {showDeleteModal ? (
          <DeleteModal
            folderId={folderId}
            folderName={folderName}
            closeMethod={() => setShowDeleteModal(false)}
          />
        ) : null}
      </div>
    </div>
  );
}
