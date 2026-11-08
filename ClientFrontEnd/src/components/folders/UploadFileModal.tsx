import { useState } from "react";

import "../../styles/folder/UploadFileModal.css";
import { uploadFile } from "../../services/fileService";
import type { FileVariant } from "../../types/fileType";
import { formatBytes } from "../../utils/formatters";

interface UploadFileModalProps {
  folderId: string;
  closeMethod: () => void;
  onUploadSuccess?: () => void;
}

const ACCEPTED_FILE_EXTENSIONS =
  ".doc,.docx,.pdf,.png,.jpg,.jpeg,.mp3,.mp4";
const MAX_FILE_SIZE_BYTES = 100 * 1024 * 1024; // 100 MB
const INVALID_FORMAT_ERROR =
  "El formato del archivo no es compatible. Formatos admitidos: Documentos (DOC, PDF), Imágenes (PNG, JPG) y Multimedia (MP3, MP4).";
const MAX_SIZE_ERROR =
  "El archivo supera el tamaño máximo permitido de 100 MB.";

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
  const [uploadProgress, setUploadProgress] = useState(0);
  const [statusMessage, setStatusMessage] = useState("");

  function handleFileChange(event: React.ChangeEvent<HTMLInputElement>) {
    const file = event.target.files?.[0] ?? null;
    setError("");
    setUploadProgress(0);
    setStatusMessage("");

    if (!file) {
      setSelectedFile(null);
      return;
    }

    if (file.size === 0) {
      setSelectedFile(file);
      setError("El archivo seleccionado está vacío.");
      return;
    }

    const fileType = getFileType(file.name);
    if (fileType === "noType") {
      setSelectedFile(file);
      setError(INVALID_FORMAT_ERROR);
      return;
    }

    if (file.size > MAX_FILE_SIZE_BYTES) {
      setSelectedFile(file);
      setError(MAX_SIZE_ERROR);
      return;
    }

    setSelectedFile(file);
    if (!fileName.trim()) {
      setFileName(file.name.replace(/\.[^/.]+$/, ""));
    }
  }

  const isFileTypeValid = selectedFile
    ? getFileType(selectedFile.name) !== "noType"
    : false;
  const isFileSizeValid = selectedFile
    ? selectedFile.size > 0 && selectedFile.size <= MAX_FILE_SIZE_BYTES
    : false;
  const canSubmit =
    Boolean(
      selectedFile && fileName.trim() && isFileTypeValid && isFileSizeValid,
    ) && !isUploading;

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

    const fileType = getFileType(selectedFile.name);
    if (fileType === "noType") {
      setError(INVALID_FORMAT_ERROR);
      return;
    }

    if (selectedFile.size === 0) {
      setError("El archivo seleccionado está vacío.");
      return;
    }

    if (selectedFile.size > MAX_FILE_SIZE_BYTES) {
      setError(MAX_SIZE_ERROR);
      return;
    }

    setError("");
    setIsUploading(true);
    setUploadProgress(0);
    setStatusMessage("Subiendo archivo...");

    try {
      await uploadFile({
        file: selectedFile,
        folderId,
        fileTypeName: fileType,
        fileName: trimmedFileName,
        onProgress: (percent) => {
          setUploadProgress(percent);
          if (percent >= 100) {
            setStatusMessage("Procesando y distribuyendo chunks en nodos...");
          } else {
            setStatusMessage(`Subiendo archivo... ${percent}%`);
          }
        },
      });

      setStatusMessage("Procesando y distribuyendo chunks en nodos...");
      await new Promise((resolve) => setTimeout(resolve, 600));

      onUploadSuccess?.();
      closeMethod();
    } catch (uploadError) {
      setError(
        uploadError instanceof Error
          ? uploadError.message
          : "No se pudo subir el archivo.",
      );
      setStatusMessage("");
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
            if (error) setError("");
          }}
          disabled={isUploading}
        />

        <label htmlFor="upload-file-input">Archivo</label>
        <input
          id="upload-file-input"
          className="upload-file-modal__file-input"
          type="file"
          accept={ACCEPTED_FILE_EXTENSIONS}
          onChange={handleFileChange}
          disabled={isUploading}
        />
        <p className="upload-file-modal__selected-file">
          {selectedFile
            ? `Archivo seleccionado: ${selectedFile.name} (${formatBytes(selectedFile.size)})`
            : "Formatos admitidos: .doc, .docx, .pdf, .png, .jpg, .jpeg, .mp3, .mp4 (máx. 100 MB)"}
        </p>

        {isUploading && (
          <div className="upload-file-modal__progress-wrapper">
            <div className="upload-file-modal__progress-info">
              <span className="upload-file-modal__progress-status">
                {statusMessage}
              </span>
              <span className="upload-file-modal__progress-percent">
                {uploadProgress}%
              </span>
            </div>
            <div
              className="upload-file-modal__progress-track"
              role="progressbar"
              aria-valuenow={uploadProgress}
              aria-valuemin={0}
              aria-valuemax={100}
            >
              <div
                className="upload-file-modal__progress-fill"
                style={{ width: `${uploadProgress}%` }}
              />
            </div>
          </div>
        )}

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
            disabled={!canSubmit}
          >
            {isUploading ? "Subiendo..." : "Subir archivo"}
          </button>
        </div>
      </form>
    </div>
  );
}
