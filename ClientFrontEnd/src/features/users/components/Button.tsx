import React from "react";
import "../styles/Button.css";

interface BotonProps {
  text: string;
  backgroundColor?: string;
  fontColor: string;
  onClick: () => void;
}

export const Button = ({
  text,
  backgroundColor,
  fontColor,
  onClick,
}: BotonProps) => {

  const finalBackground = backgroundColor === undefined ? 'transparent' : backgroundColor;

  return (
    <button
      className="btn"
      onClick={onClick}
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
