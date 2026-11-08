import "../../styles/folder/DeleteModal.css";
import { deleteFile } from "../../services/fileService";

interface DeleteFileModalProps {
  fileId: string;
  fileName: string;
  closeMethod: () => void;
}

export function DeleteFileModal({
  fileId,
  fileName,
  closeMethod,
}: DeleteFileModalProps) {
  return (
    <div className="delete-modal-overlay">
      <div className="delete-modal-content">
        <svg
          xmlns="http://www.w3.org/2000/svg"
          viewBox="0 0 24 24"
          className="delete-modal__svg"
        >
          <path d="m21.73 18-8-14a2 2 0 0 0-3.48 0l-8 14A2 2 0 0 0 4 21h16a2 2 0 0 0 1.73-3" />
          <path d="M12 9v4" />
          <path d="M12 17h.01" />
        </svg>
        <h2 className="delete-modal__title">Borrar Archivo</h2>
        <p className="delete-modal__text">Seguro que quieres borrar el archivo "<span>{fileName}</span>".</p>
        <div className="delete-modal__button-wrapper">
          <button
            className="delete-modal__button"
            onClick={() => closeMethod()}
            type="button"
          >
            No, mantener archivo
          </button>
          <button
            className="delete-modal__button delete-modal__button--yes"
            onClick={() => deleteFile(fileId)}
            type="button"
          >
            Si, borrar archivo
          </button>
        </div>
      </div>
    </div>
  );
}
