import { useState } from "react";

import "../../styles/folder/UploadFileModal.css";
import { uploadFile } from "../../services/fileService";
import type { FileVariant } from "../../types/fileType";

interface UploadFileModalProps {
  folderId: string;
  closeMethod: () => void;
  onUploadSuccess?: () => void;
}

function getFileType(fileName: string): FileVariant {
  const extension = fileName.split(".").pop()?.toLowerCase();

  switch (extension) {
    case "doc":
    case "docx":
      return "doc";
    case "pdf":
      return "pdf";
    case "png":
      return "png";
    case "jpg":
    case "jpeg":
      return "jpg";
    case "mp3":
      return "mp3";
    case "mp4":
      return "mp4";
    default:
      return "noType";
  }
}

export function UploadFileModal({
  folderId,
  closeMethod,
  onUploadSuccess,
}: UploadFileModalProps) {
  const [fileName, setFileName] = useState("");
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [error, setError] = useState("");
  const [isUploading, setIsUploading] = useState(false);

  function handleFileChange(event: React.ChangeEvent<HTMLInputElement>) {
    const file = event.target.files?.[0] ?? null;
    setSelectedFile(file);
    setError("");

    if (file && !fileName.trim()) {
      setFileName(file.name.replace(/\.[^/.]+$/, ""));
    }
  }

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const trimmedFileName = fileName.trim();

    if (!trimmedFileName) {
      setError("Debes ingresar un nombre para el archivo.");
      return;
    }

    if (!selectedFile) {
      setError("Debes seleccionar un archivo.");
      return;
    }

    setError("");
    setIsUploading(true);

    try {
      await uploadFile({
        file: selectedFile,
        folderId,
        fileTypeName: getFileType(selectedFile.name),
        fileName: trimmedFileName,
      });
      onUploadSuccess?.();
      closeMethod();
    } catch (uploadError) {
      setError(
        uploadError instanceof Error
          ? uploadError.message
          : "No se pudo subir el archivo.",
      );
    } finally {
      setIsUploading(false);
    }
  }

  return (
    <div className="upload-file-modal-overlay">
      <form className="upload-file-modal-content" onSubmit={handleSubmit}>
        <h2>Subir archivo</h2>
        <p>Asigna un nombre y selecciona el archivo que deseas subir.</p>

        <label htmlFor="upload-file-name">Nombre del archivo</label>
        <input
          id="upload-file-name"
          className="upload-file-modal__input"
          type="text"
          value={fileName}
          placeholder="Ingresa el nombre del archivo"
          onChange={(event) => {
            setFileName(event.target.value);
            setError("");
          }}
          disabled={isUploading}
        />

        <label htmlFor="upload-file-input">Archivo</label>
        <input
          id="upload-file-input"
          className="upload-file-modal__file-input"
          type="file"
          onChange={handleFileChange}
          disabled={isUploading}
        />
        <p className="upload-file-modal__selected-file">
          {selectedFile ? `Archivo seleccionado: ${selectedFile.name}` : "No has seleccionado un archivo."}
        </p>

        {error && <p className="upload-file-modal__error">{error}</p>}

        <div className="upload-file-modal__button-wrapper">
          <button
            className="upload-file-modal__button"
            type="button"
            onClick={closeMethod}
            disabled={isUploading}
          >
            Cancelar
          </button>
          <button
            className="upload-file-modal__button upload-file-modal__button--submit"
            type="submit"
            disabled={isUploading}
          >
            {isUploading ? "Subiendo..." : "Subir archivo"}
          </button>
        </div>
      </form>
    </div>
  );
}
