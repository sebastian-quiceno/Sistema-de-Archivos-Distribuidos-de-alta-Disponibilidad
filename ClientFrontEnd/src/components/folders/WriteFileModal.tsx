import { useState } from "react";
import "../../styles/folder/UploadFileModal.css";
import { updateFile } from "../../services/fileService";

interface WriteFileModalProps {
  fileId: string;
  fileName: string;
  closeMethod: () => void;
  onUpdateSuccess?: () => void;
}

export function WriteFileModal({
  fileId,
  fileName,
  closeMethod,
  onUpdateSuccess,
}: WriteFileModalProps) {
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [error, setError] = useState("");
  const [isUpdating, setIsUpdating] = useState(false);

  function handleFileChange(event: React.ChangeEvent<HTMLInputElement>) {
    const file = event.target.files?.[0] ?? null;
    setSelectedFile(file);
    setError("");
  }

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();

    if (!selectedFile) {
      setError("Debes seleccionar un archivo para actualizar.");
      return;
    }

    if (selectedFile.size === 0) {
      setError("El archivo seleccionado no puede estar vacío.");
      return;
    }

    setError("");
    setIsUpdating(true);

    try {
      await updateFile({
        fileId,
        file: selectedFile,
      });

      onUpdateSuccess?.();
      closeMethod();
    } catch (updateError) {
      setError(
        updateError instanceof Error
          ? updateError.message
          : "No se pudo actualizar el archivo.",
      );
    } finally {
      setIsUpdating(false);
    }
  }

  return (
    <div className="upload-file-modal-overlay">
      <form className="upload-file-modal-content" onSubmit={handleSubmit}>
        <h2>Actualizar archivo</h2>
        <p>
          Selecciona un nuevo contenido para reemplazar "<span>{fileName}</span>".
        </p>

        <label htmlFor="update-file-input">Nuevo archivo</label>
        <input
          id="update-file-input"
          className="upload-file-modal__file-input"
          type="file"
          onChange={handleFileChange}
          disabled={isUpdating}
        />
        <p className="upload-file-modal__selected-file">
          {selectedFile
            ? `Archivo seleccionado: ${selectedFile.name}`
            : "No has seleccionado un archivo."}
        </p>

        {error && <p className="upload-file-modal__error">{error}</p>}

        <div className="upload-file-modal__button-wrapper">
          <button
            className="upload-file-modal__button"
            type="button"
            onClick={closeMethod}
            disabled={isUpdating}
          >
            Cancelar
          </button>
          <button
            className="upload-file-modal__button upload-file-modal__button--submit"
            type="submit"
            disabled={isUpdating || !selectedFile}
          >
            {isUpdating ? "Actualizando..." : "Actualizar archivo"}
          </button>
        </div>
      </form>
    </div>
  );
}
