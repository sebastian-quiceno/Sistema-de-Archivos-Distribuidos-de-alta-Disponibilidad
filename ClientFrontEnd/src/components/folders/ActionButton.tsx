import "../../styles/folder/ActionButton.css";

type ButtonVariant = "write" | "download" | "delete";

interface ActionButtonProps {
  variant: ButtonVariant;
  onClick: () => void;
  disabled?: boolean;
}

export function ActionButton({ variant, onClick, disabled }: ActionButtonProps) {
  const Icon = iconMap[variant];

  return (
    <button className="acction-button" type="button" onClick={onClick} disabled = {disabled}>
      <Icon />
    </button>
  );
}


const WriteSVG = () => {
  return (
    <svg
      xmlns="http://www.w3.org/2000/svg"
      viewBox="0 0 24 24"
      className="action-svg action-svg__write"
    >
      <path d="M13 21h8" />
      <path d="m15 5 4 4" />
      <path d="M21.174 6.812a1 1 0 0 0-3.986-3.987L3.842 16.174a2 2 0 0 0-.5.83l-1.321 4.352a.5.5 0 0 0 .623.622l4.353-1.32a2 2 0 0 0 .83-.497z" />
    </svg>
  );
};

const DownloadSVG = () => {
  return (
    <svg
      xmlns="http://www.w3.org/2000/svg"
      viewBox="0 0 24 24"
      className="action-svg action-svg__download"
    >
      <path d="M12 15V3" />
      <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" />
      <path d="m7 10 5 5 5-5" />
    </svg>
  );
};

const DeleteSVG = () => {
  return (
    <svg
      xmlns="http://www.w3.org/2000/svg"
      viewBox="0 0 24 24"
      className="action-svg action-svg__delete"
    >
      <path d="M10 11v6" />
      <path d="M14 11v6" />
      <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6" />
      <path d="M3 6h18" />
      <path d="M8 6V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" />
    </svg>
  );
};

const iconMap: Record<ButtonVariant, React.ComponentType> = {
  write: WriteSVG,
  download: DownloadSVG,
  delete: DeleteSVG,
};