import { createContext, useContext, useState, useEffect } from 'react';
import AsyncStorage from '@react-native-async-storage/async-storage';

type ScreenTimeType = {
  id: number,
  time: number,
  app: string,
}

const ScreenTimeContext = createContext<{
  screenTimeData: ScreenTimeType[],
  addScreenTime: (time: number, app: string) => void,
  removeScreenTime: (id: number) => void
} | null>(null);

export function ScreenTimeProvider({ children }: { children: React.ReactNode }) {
  const [screenTimeData, setScreenTime] = useState<ScreenTimeType[]>([]);

  // Load from storage when app opens
  useEffect(() => {
    const load = async () => {
      const saved = await AsyncStorage.getItem('screenTime');
      if (saved) setScreenTime(JSON.parse(saved));
    };
    load();
  }, []);

  // Save to storage whenever list changes
  useEffect(() => {
    AsyncStorage.setItem('screenTime', JSON.stringify(screenTimeData));
  }, [screenTimeData]);

  const addScreenTime = (time: number, app: string) => {
    const newList = [...screenTimeData, {
      id: Math.random(),
      time,
      app
    }];
    setScreenTime(newList); 
  };

  const removeScreenTime = (id: number) => {
    const newList = screenTimeData.filter(x => x.id !== id);
    setScreenTime(newList);
  };

  return (
    <ScreenTimeContext.Provider value={{ screenTimeData, addScreenTime, removeScreenTime}}>
      {children}
    </ScreenTimeContext.Provider>
  );
}

export function useScreenTime() {
  return useContext(ScreenTimeContext)!;
}