import "../../styles/folder/FileBlock.css";

import { useState } from "react";

import { ActionButton } from "./ActionButton";
import { File } from "./File";
import { type FileVariant } from "../../types/fileType";

import { DeleteModal } from "../../components/folders/DeleteModal";
import { WriteModal } from "../../components/folders/WriteModal";

interface FileBlockProps {
  fileId: string;
  fileName: string;
  fileType: FileVariant;
  modifiedDate: Date;
  size: number;
}

export function FileBlock({
  fileId,
  fileName,
  fileType,
  modifiedDate,
  size,
}: FileBlockProps) {
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [showWriteModal, setShowWriteModal] = useState(false);

  return (
    <div className="file-block">
      <div className="folder-block__file-wrapper">
        <File fileName={fileName} fileType={fileType} />
      </div>

      <p className="file-block__text">{modifiedDate.getDate()}</p>
      <p className="file-block__text">{size}</p>

      <div className="file-block-actions">
        <ActionButton onClick={() => setShowWriteModal(true)} variant="write" />
        {showWriteModal ? (
          <WriteModal
            folderId={fileId}
            folderName={fileName}
            closeMethod={() => setShowWriteModal(false)}
          />
        ) : null}

        <ActionButton onClick={() => null} variant="download" />

        <ActionButton
          onClick={() => setShowDeleteModal(true)}
          variant="delete"
        />
        {showDeleteModal ? (
          <DeleteModal
            folderId={fileId}
            folderName={fileName}
            closeMethod={() => setShowDeleteModal(false)}
          />
        ) : null}
      </div>
    </div>
  );
}
