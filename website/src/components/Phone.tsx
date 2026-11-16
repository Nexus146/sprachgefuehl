type PhoneProps = {
  src: string;
  alt: string;
  className?: string;
  priority?: boolean;
};

export function Phone({ src, alt, className = '', priority = false }: PhoneProps) {
  return (
    <div className={`phone ${className}`.trim()}>
      <div className="phone__screen">
        <img
          src={src}
          alt={alt}
          width={1080}
          height={2280}
          loading={priority ? 'eager' : 'lazy'}
          decoding="async"
        />
      </div>
    </div>
  );
}
