import '../../styles/folder/ButtonWithIcon.css'
import type { CSSProperties, ComponentType, SVGProps } from 'react'

interface ButtonWithIconProps {
  text: string;
  Icono: ComponentType<SVGProps<SVGSVGElement>>;
  background: string;
  color: string;
  colorIco: string;
  onClick?: () => void;
  disabled?: boolean;
  className?: string;
}

export function ButtonWithIcon({
  text,
  Icono,
  background,
  color,
  colorIco,
  onClick,
  disabled = false,
  className = '',
}: ButtonWithIconProps) {
  const buttonStyles = {
    '--color-button-with-icon-background': background,
    '--color-button-with-icon-text': color,
    '--color-button-with-icon-ico': colorIco,
  } as CSSProperties;

  return (
    <button
      className={`button-with-icon ${className}`.trim()}
      type="button"
      style={buttonStyles}
      onClick={onClick}
      disabled={disabled}
    >
      <Icono className="button-with-icon__ico" />
      <p className="button-with-icon__text">{text}</p>
    </button>
  );
}
