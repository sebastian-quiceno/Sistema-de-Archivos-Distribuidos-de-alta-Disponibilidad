import { useState } from "react";
import "../../styles/folder/WriteModal.css";
import { createFolder } from "../../services/folderService";

interface CreateFolderModalProps {
  fatherId: string;
  closeMethod: () => void;
  onCreateSuccess?: () => void;
}

export function CreateFolderModal({
  fatherId,
  closeMethod,
  onCreateSuccess,
}: CreateFolderModalProps) {
  const [folderName, setFolderName] = useState("");
  const [error, setError] = useState("");
  const [isCreating, setIsCreating] = useState(false);

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const trimmed = folderName.trim();
    if (!trimmed) {
      setError("El nombre de la carpeta no puede estar vacío.");
      return;
    }

    setError("");
    setIsCreating(true);

    try {
      await createFolder({
        folderName: trimmed,
        fatherId: fatherId.trim() || null,
      });

      onCreateSuccess?.();
      closeMethod();
    } catch (err: unknown) {
      setError(
        err instanceof Error ? err.message : "Error al crear la carpeta.",
      );
    } finally {
      setIsCreating(false);
    }
  }

  return (
    <div className="rename-modal-overlay">
      <form className="rename-modal-content" onSubmit={handleSubmit}>
        <svg
          xmlns="http://www.w3.org/2000/svg"
          viewBox="0 0 24 24"
          className="rename-modal__svg"
          fill="none"
          stroke="var(--color-icon-file-folder, #f0c785)"
          strokeWidth="2"
          strokeLinecap="round"
          strokeLinejoin="round"
        >
          <path d="M20 20a2 2 0 0 0 2-2V8a2 2 0 0 0-2-2h-7.9a2 2 0 0 1-1.69-.9L9.6 3.9A2 2 0 0 0 7.93 3H4a2 2 0 0 0-2 2v13a2 2 0 0 0 2 2Z" />
          <path d="M12 10v6" />
          <path d="M9 13h6" />
        </svg>
        <h2>Nueva Carpeta</h2>
        <p>Ingresa el nombre de la nueva carpeta</p>
        <div className="rename-modal__input-wrapper">
          <input
            name="folderName"
            className="rename-modal__input"
            type="text"
            placeholder="Nombre de la carpeta"
            value={folderName}
            disabled={isCreating}
            onChange={(event) => {
              setFolderName(event.target.value);
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
            disabled={isCreating}
            type="button"
          >
            Cancelar
          </button>
          <button
            className="rename-modal__button rename-modal__button--save"
            type="submit"
            disabled={isCreating}
          >
            {isCreating ? "Creando..." : "Crear"}
          </button>
        </div>
      </form>
    </div>
  );
}
