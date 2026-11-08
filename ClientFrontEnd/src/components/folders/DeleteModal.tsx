import { useState } from "react";
import "../../styles/folder/DeleteModal.css";
import { deleteFolder } from "../../services/folderService";

interface DeleteModalProps {
  folderId: string;
  folderName: string;
  closeMethod: () => void;
  onDeleteSuccess?: (folderId: string) => void;
}

export function DeleteModal({
  folderId,
  closeMethod,
  folderName,
  onDeleteSuccess,
}: DeleteModalProps) {
  const [isDeleting, setIsDeleting] = useState(false);
  const [error, setError] = useState("");

  async function handleDelete() {
    setError("");
    setIsDeleting(true);

    try {
      await deleteFolder(folderId);
      onDeleteSuccess?.(folderId);
      closeMethod();
    } catch (err: unknown) {
      setError(
        err instanceof Error ? err.message : "Error al eliminar la carpeta",
      );
    } finally {
      setIsDeleting(false);
    }
  }

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
        <h2 className="delete-modal__title">Borrar Carpeta</h2>
        <p className="delete-modal__text">
          ¿Seguro que quieres borrar la carpeta "<span>{folderName}</span>"?
        </p>
        {error && <p className="delete-modal__error">{error}</p>}
        <div className="delete-modal__button-wrapper">
          <button
            className="delete-modal__button"
            onClick={closeMethod}
            disabled={isDeleting}
            type="button"
          >
            Cancelar
          </button>
          <button
            className="delete-modal__button delete-modal__button--yes"
            onClick={handleDelete}
            disabled={isDeleting}
            type="button"
          >
            {isDeleting ? "Borrando..." : "Sí, borrar carpeta"}
          </button>
        </div>
      </div>
    </div>
  );
}
