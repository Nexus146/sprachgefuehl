/**
 * Der Sprachgarten aus der App: Stiel, vier Blätter, Topf – und am Ende eine Blüte.
 * `progress` (0–1) steuert das Wachstum; die Stufen heißen wie in der App.
 */

const clamp = (value: number) => Math.min(1, Math.max(0, value));

export function gardenStage(progress: number): string {
  if (progress < 0.22) return 'Samen';
  if (progress < 0.48) return 'Setzling';
  if (progress < 0.8) return 'Knospen';
  return 'Blüte';
}

const LEAVES = [
  { y: 206, side: -1, rotate: -20, index: 0 },
  { y: 176, side: 1, rotate: 20, index: 1 },
  { y: 146, side: -1, rotate: -20, index: 2 },
  { y: 116, side: 1, rotate: 20, index: 3 },
];

const PETALS = [0, 72, 144, 216, 288].map((angle) => ({
  angle,
  cx: 110 + Math.cos((angle * Math.PI) / 180) * 13,
  cy: 84 + Math.sin((angle * Math.PI) / 180) * 13,
}));

type GardenProps = {
  progress: number;
  className?: string;
};

export function Garden({ progress, className }: GardenProps) {
  const grow = clamp(progress);
  const bloom = clamp((grow - 0.8) / 0.2);
  const stemLength = 168;

  return (
    <svg
      className={className}
      viewBox="0 0 220 300"
      role="img"
      aria-label={`Sprachgarten in der Stufe ${gardenStage(grow)}`}
    >
      <ellipse cx="110" cy="286" rx="54" ry="7" fill="var(--line)" opacity="0.7" />

      <path
        d="M110 246 C 108 214, 112 152, 110 84"
        fill="none"
        stroke="var(--garden-stem)"
        strokeWidth="6"
        strokeLinecap="round"
        strokeDasharray={stemLength}
        strokeDashoffset={stemLength * (1 - grow)}
      />

      {LEAVES.map((leaf) => {
        const scale = clamp((grow - (0.12 + leaf.index * 0.18)) / 0.24);
        const cx = 110 + leaf.side * 31;
        return (
          <g
            key={leaf.y}
            style={{
              transformBox: 'view-box',
              transformOrigin: `110px ${leaf.y}px`,
              transform: `scale(${scale})`,
            }}
          >
            <ellipse
              cx={cx}
              cy={leaf.y}
              rx={27}
              ry={11}
              fill="var(--garden-leaf)"
              transform={`rotate(${leaf.rotate} ${cx} ${leaf.y})`}
            />
          </g>
        );
      })}

      <g
        style={{
          transformBox: 'view-box',
          transformOrigin: '110px 84px',
          transform: `scale(${bloom})`,
        }}
      >
        {PETALS.map((petal) => (
          <circle key={petal.angle} cx={petal.cx} cy={petal.cy} r="9.5" fill="var(--garden-bloom)" />
        ))}
        <circle cx="110" cy="84" r="5.5" fill="var(--garden-pot)" />
      </g>

      <rect x="74" y="244" width="72" height="42" rx="12" fill="var(--garden-pot)" />
      <rect x="68" y="236" width="84" height="14" rx="7" fill="var(--garden-pot)" />
    </svg>
  );
}
