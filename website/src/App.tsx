import { ChapterExplorer } from './components/ChapterExplorer';
import { Craft } from './components/Craft';
import { Faq } from './components/Faq';
import { Footer } from './components/Footer';
import { GlossaryDemo } from './components/GlossaryDemo';
import { Hero } from './components/Hero';
import { Install } from './components/Install';
import { LessonSteps } from './components/LessonSteps';
import { Nav } from './components/Nav';
import { Numbers } from './components/Numbers';
import { PhoneTour } from './components/PhoneTour';
import { Practice } from './components/Practice';
import { PrivacyStrip } from './components/PrivacyStrip';
import { Science } from './components/Science';
import { Stand } from './components/Stand';

export function App() {
  return (
    <>
      <a className="skip-link" href="#lernweg">
        Zum Inhalt springen
      </a>
      <Nav />
      <main>
        <Hero />
        <LessonSteps />
        <PhoneTour />
        <ChapterExplorer />
        <Practice />
        <Stand />
        <GlossaryDemo />
        <Science />
        <Craft />
        <Numbers />
        <Install />
        <PrivacyStrip />
        <Faq />
      </main>
      <Footer />
    </>
  );
}
