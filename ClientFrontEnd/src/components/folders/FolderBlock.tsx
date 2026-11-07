import {useState} from 'react'

import { ActionButton } from "./ActionButton";
import { File, type FileVariant } from "./File";
import {deleteFolder, getSubfolders, moveFolder, renameFolder} from "../../services/folderService";

import {DeleteModal} from "../../components/folders/DeleteModal";


interface FolderBlockProps {
  fileId: string;
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
}: FolderBlockProps) {
  const [showDeleteModal, setShowDeleteModal] = useState(false)
  

  return (
    <div className="file-block">
      <File fileName={fileName} fileType="folder"  />
      <p>{modifiedDate.getDate()}</p>
      <p>. . .</p>

      <div className="file-block-actions">
        <ActionButton onClick={} variant="write" />

        <ActionButton onClick={() => null} variant="download" disabled />

        <ActionButton onClick={} variant="delete" />
      </div>
    </div>
  );
}
