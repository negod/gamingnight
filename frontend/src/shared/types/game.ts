export type Game = {
  id: string;
  name: string;
  gameType: 'SCORE_BASED' | 'TIME_BASED';
  calculationMethod: 'SUM' | 'AVERAGE';
  description: string;
};

export type GameFormValues = {
  name: string;
  gameType: 'SCORE_BASED' | 'TIME_BASED';
  calculationMethod: 'SUM' | 'AVERAGE';
  description: string;
};
