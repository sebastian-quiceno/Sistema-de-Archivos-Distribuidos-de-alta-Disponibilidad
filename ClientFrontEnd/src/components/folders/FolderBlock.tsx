import { useState } from "react";
import "../../styles/folder/FolderBlock.css";

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
  folderName: string;
  modifiedDate: Date;
}

export function FolderBlock({
  folderId,
  folderName,
  modifiedDate,
}: FolderBlockProps) {
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [showWriteModal, setShowWriteModal] = useState(false);

  return (
    <div className="folder-block">
      <div className="folder-block__file-wrapper">
        <File fileName={folderName} fileType="folder" />
      </div>
      <p className="folder-block__text">{modifiedDate.getDate()}</p>
      <p className="folder-block__text">. . .</p>

      <div className="folder-block-actions">
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
