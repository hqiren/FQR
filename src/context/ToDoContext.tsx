import { createContext, useContext, useState, useEffect } from 'react';
import AsyncStorage from '@react-native-async-storage/async-storage';

type ToDoType = {
  id: number,
  task: string,
  isDone: boolean,
}

const ToDoContext = createContext<{
  toDoData: ToDoType[],
  addTask: (task: string) => void,
  removeTask: (id: number) => void
  handleDone: (id: number) => void
} | null>(null);

export function ToDoProvider({ children }: { children: React.ReactNode }) {
  const [toDoData, setToDo] = useState<ToDoType[]>([]);

  // Load from storage when app opens
  useEffect(() => {
    const load = async () => {
      const saved = await AsyncStorage.getItem('todos');
      if (saved) setToDo(JSON.parse(saved));
    };
    load();
  }, []);

  // Save to storage whenever list changes
  useEffect(() => {
    AsyncStorage.setItem('todos', JSON.stringify(toDoData));
  }, [toDoData]);

  const addTask = (task: string) => {
    const newList = [...toDoData, {
      id: Math.random(),
      task,
      isDone: false
    }];
    setToDo(newList); 
  };

  const removeTask = (id: number) => {
    const newList = toDoData.filter(x => x.id !== id);
    setToDo(newList);
  };

  const handleDone = (id: number) => {
    const newList = toDoData.map(todo => {
        if (todo.id == id) {
            todo.isDone = !todo.isDone;
        }
        return todo;
  });
    setToDo(newList);
  }

  return (
    <ToDoContext.Provider value={{ toDoData, addTask, removeTask, handleDone }}>
      {children}
    </ToDoContext.Provider>
  );
}

export function useToDo() {
  return useContext(ToDoContext)!;
}