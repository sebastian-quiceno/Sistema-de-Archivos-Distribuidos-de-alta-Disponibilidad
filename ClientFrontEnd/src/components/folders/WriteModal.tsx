import "../../styles/folder/WriteModal.css";
import { deleteFolder } from "../../services/folderService";

interface WriteModalProps {
  folderId: string;
  folderName: string;
  closeMethod: () => void;
}

export function WriteModal({
  folderId,
  closeMethod,
  folderName,
}: WriteModalProps) {
  return (
    <div className="rename-modal-overlay">
      <div className="rename-modal-content">
        <svg
          xmlns="http://www.w3.org/2000/svg"
          viewBox="0 0 24 24"
          className="rename-modal__svg"
        >
          <path d="m21.73 18-8-14a2 2 0 0 0-3.48 0l-8 14A2 2 0 0 0 4 21h16a2 2 0 0 0 1.73-3" />
          <path d="M12 9v4" />
          <path d="M12 17h.01" />
        </svg>
        <h2>Borrar Carpeta</h2>
        <p>Seguro que quieres borrar la carpeta "<span>{folderName}</span>".</p>
        <div className="rename-modal__button-wrapper">
          <button
            className="rename-modal__button"
            onClick={() => closeMethod()}
            type="button"
          >
            No, mantener carpeta
          </button>
          <button
            className="rename-modal__button rename-modal__button--yes"
            onClick={() => deleteFolder(folderId)}
            type="button"
          >
            Si, borrar carpeta
          </button>
        </div>
      </div>
    </div>
  );
}
