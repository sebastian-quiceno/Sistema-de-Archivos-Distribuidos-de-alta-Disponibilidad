import { useState } from "react";

import "../../styles/folder/WriteModal.css";
import { renameFile } from "../../services/fileService";

interface WriteFileModalProps {
  fileId: string;
  fileName: string;
  closeMethod: () => void;
  onRenameSuccess?: (newFileName: string) => void;
  onUpdateSuccess?: () => void;
}

export function WriteFileModal({
  fileId,
  fileName,
  closeMethod,
  onRenameSuccess,
  onUpdateSuccess,
}: WriteFileModalProps) {
  const [newFileName, setNewFileName] = useState<string>(fileName);
  const [error, setError] = useState("");
  const [isRenaming, setIsRenaming] = useState(false);

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const trimmed = newFileName.trim();
    if (!trimmed) {
      setError("El nombre del archivo no puede estar vacío.");
      return;
    }

    setError("");
    setIsRenaming(true);

    try {
      await renameFile({
        fileId,
        newFileName: trimmed,
      });

      onRenameSuccess?.(trimmed);
      onUpdateSuccess?.();
      closeMethod();
    } catch (err: unknown) {
      setError(
        err instanceof Error
          ? err.message
          : "No se pudo cambiar el nombre del archivo",
      );
    } finally {
      setIsRenaming(false);
    }
  }

  return (
    <div className="rename-modal-overlay">
      <form className="rename-modal-content" onSubmit={handleSubmit}>
        <svg
          xmlns="http://www.w3.org/2000/svg"
          viewBox="0 0 24 24"
          className="rename-modal__svg"
        >
          <path d="M12.659 22H18a2 2 0 0 0 2-2V8a2.4 2.4 0 0 0-.706-1.706l-3.588-3.588A2.4 2.4 0 0 0 14 2H6a2 2 0 0 0-2 2v9.34" />
          <path d="M14 2v5a1 1 0 0 0 1 1h5" />
          <path d="M10.378 12.622a1 1 0 0 1 3 3.003L8.36 20.637a2 2 0 0 1-.854.506l-2.867.837a.5.5 0 0 1-.62-.62l.836-2.869a2 2 0 0 1 .506-.853z" />
        </svg>
        <h2>Renombrar Archivo</h2>
        <p>
          Coloca el nuevo nombre del archivo "<span>{fileName}</span>"
        </p>
        <div className="rename-modal__input-wrapper">
          <input
            name="newFileName"
            className="rename-modal__input"
            type="text"
            placeholder="Ingrese el nuevo nombre"
            value={newFileName}
            disabled={isRenaming}
            onChange={(event) => {
              setNewFileName(event.target.value);
              if (error) setError("");
            }}
            autoFocus
          />
        </div>
        {error && (
          <p
            className="rename-modal__error"
            style={{
              color: "var(--color-input-error)",
              fontSize: "0.85rem",
              fontWeight: 500,
              textAlign: "center",
            }}
          >
            {error}
          </p>
        )}
        <div className="rename-modal__button-wrapper">
          <button
            className="rename-modal__button"
            onClick={closeMethod}
            disabled={isRenaming}
            type="button"
          >
            Cancelar
          </button>
          <button
            className="rename-modal__button rename-modal__button--save"
            type="submit"
            disabled={isRenaming}
          >
            {isRenaming ? "Guardando..." : "Renombrar"}
          </button>
        </div>
      </form>
    </div>
  );
}
