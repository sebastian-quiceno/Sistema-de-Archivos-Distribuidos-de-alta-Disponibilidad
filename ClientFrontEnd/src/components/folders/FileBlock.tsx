import { ActionButton } from "./ActionButton";
import { File, type FileVariant } from "./File";
import {} from "../../services/userService";

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
  return (
    <div className="file-block">
      <File fileName={fileName} fileType={fileType} />
      <p>{modifiedDate.getDate()}</p>
      <p>{size}</p>
      <div className="file-block-actions">
        <ActionButton onClick={()=> null} variant="write" />

        {fileType === "folder" ? (
          <ActionButton onClick={() => null} variant="download" disabled />
        ) : (
          <ActionButton onClick={()=> null} variant="download" />
        )}

        <ActionButton onClick={()=> null} variant="delete" />
      </div>
    </div>
  );
}
