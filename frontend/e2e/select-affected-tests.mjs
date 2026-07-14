import { spawnSync } from 'node:child_process';

const before = process.env.GITHUB_EVENT_BEFORE || process.env.GITHUB_BEFORE;
const sha = process.env.GITHUB_SHA || 'HEAD';

function changedFiles() {
  if (!before || /^0+$/.test(before)) {
    return [];
  }

  const result = spawnSync('git', ['diff', '--name-only', before, sha], {
    encoding: 'utf8',
  });

  if (result.status !== 0) {
    throw new Error(result.stderr || 'Unable to read changed files for affected E2E selection');
  }

  return result.stdout.split('\n').map((line) => line.trim()).filter(Boolean);
}

const fullSuitePatterns = [
  /^\.github\/workflows\//,
  /^frontend\/e2e\//,
  /^frontend\/playwright\.config\.ts$/,
  /^frontend\/package(-lock)?\.json$/,
  /^frontend\/src\/app\//,
  /^frontend\/src\/main\.tsx$/,
  /^frontend\/src\/shared\/api\//,
  /^frontend\/src\/shared\/auth\//,
  /^frontend\/src\/pages\/LoginPage\.tsx$/,
  /^backend\/src\/main\/java\/se\/backede\/infrastructure\/security\//,
  /^backend\/src\/main\/java\/se\/backede\/infrastructure\/web\/GlobalExceptionHandler\.java$/,
  /^backend\/src\/main\/resources\/application/,
];

const tagMappings = [
  { tag: '@auth', patterns: [/^frontend\/src\/features\/auth\//, /^backend\/src\/main\/java\/se\/backede\/.*\/Auth/] },
  { tag: '@users', patterns: [/^frontend\/src\/features\/users\//, /^frontend\/src\/pages\/CurrentUserPage\.tsx$/, /^frontend\/src\/pages\/UsersPage\.tsx$/, /^backend\/src\/main\/java\/se\/backede\/.*\/User/] },
  { tag: '@players', patterns: [/^frontend\/src\/features\/players\//, /^frontend\/src\/pages\/PlayersPage\.tsx$/, /^backend\/src\/main\/java\/se\/backede\/.*\/Player/] },
  { tag: '@games', patterns: [/^frontend\/src\/features\/games\//, /^frontend\/src\/pages\/GamesPage\.tsx$/, /^backend\/src\/main\/java\/se\/backede\/.*\/Game/] },
  { tag: '@teams', patterns: [/^frontend\/src\/features\/teams\//, /^frontend\/src\/pages\/TeamsPage\.tsx$/, /^backend\/src\/main\/java\/se\/backede\/.*\/Team/] },
  { tag: '@competitions', patterns: [/^frontend\/src\/features\/competitions\//, /^frontend\/src\/pages\/CompetitionsPage\.tsx$/, /^backend\/src\/main\/java\/se\/backede\/.*\/Competition(?!Run)/] },
  { tag: '@competition-run', patterns: [/^frontend\/src\/features\/competition-run\//, /^frontend\/src\/pages\/CompetitionRunPage\.tsx$/, /^backend\/src\/main\/java\/se\/backede\/.*\/CompetitionRun/, /^backend\/src\/main\/java\/se\/backede\/.*\/Match/] },
  { tag: '@leaderboards', patterns: [/Leaderboard/, /^frontend\/src\/shared\/types\/leaderboard\.ts$/] },
];

const files = changedFiles();
const runFullSuite = files.length === 0 || files.some((file) => fullSuitePatterns.some((pattern) => pattern.test(file)));
const args = ['playwright', 'test'];

if (!runFullSuite) {
  const tags = new Set(['@smoke']);
  for (const file of files) {
    for (const mapping of tagMappings) {
      if (mapping.patterns.some((pattern) => pattern.test(file))) {
        tags.add(mapping.tag);
      }
    }
  }
  args.push('--grep', Array.from(tags).join('|'));
}

console.log(runFullSuite ? 'Running full E2E suite.' : `Running affected E2E tags: ${args.at(-1)}`);
const result = spawnSync('npx', args, { stdio: 'inherit', shell: process.platform === 'win32' });
process.exit(result.status ?? 1);
