import React from "react";
import "../../styles/user/Button.css";

interface BotonProps {
  text: string;
  backgroundColor?: string;
  fontColor: string;
  isSubmit?: boolean;
  disabled?: boolean;
  onClick?: () => void;
}

export const Button = ({
  text,
  backgroundColor,
  fontColor,
  isSubmit = false,
  disabled = false,
  onClick,
}: BotonProps) => {
  const finalBackground = backgroundColor === undefined ? "transparent" : backgroundColor;

  return (
    <button
      className="btn"
      disabled={disabled}
      onClick={onClick}
      type={isSubmit ? "submit" : "button"}
      style={
        {
          "--bg-color": finalBackground,
          "--text-color": fontColor,
        } as React.CSSProperties
      }
    >
      {text}
    </button>
  );
};
