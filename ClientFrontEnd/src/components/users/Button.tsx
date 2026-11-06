import React from "react";
import "../../styles/user/Button.css";

interface BotonProps {
  text: string;
  backgroundColor?: string;
  fontColor: string;
  isSubmit?: boolean;
  onClick?: () => void;
}

export const Button = ({
  text,
  backgroundColor,
  fontColor,
  isSubmit,
  onClick,
}: BotonProps) => {

  const finalBackground = backgroundColor === undefined ? 'transparent' : backgroundColor;

  return (
    <button
      className="btn"
      onClick={onClick? onClick: ( () => null )} 
      type={(isSubmit === null || isSubmit === false)? "button": "submit"}
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
