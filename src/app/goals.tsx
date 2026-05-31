import { View, Text, TouchableOpacity, StyleSheet, FlatList, TextInput } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { Checkbox } from 'expo-checkbox';
import { Ionicons } from '@expo/vector-icons';
import { router } from 'expo-router';
import { useState } from 'react';
import { useToDo } from '../context/ToDoContext';

const styles = StyleSheet.create({
  container: { flex: 1 },
  headerText: {
    fontWeight: 'bold',
    fontSize: 20,
    fontFamily: 'Georgia',
    textAlign: 'center',
    marginBottom: 20
  },
  toDoContainer: {
    backgroundColor: '#fff',
    padding: 16,
    borderRadius: 16,
    marginBottom: 10,
    marginHorizontal: 16,
  },
  toDoInfoContainer: {
    flexDirection: 'row',
    gap: 4,
    alignItems: 'center',
    justifyContent: 'space-between'
  },
  backButton: {
    flexDirection: 'row',
    justifyContent: 'flex-start',
    alignItems: 'center',
    paddingVertical: 8,
    paddingHorizontal: 16,
  },
  inputRow: {
    flexDirection: 'row',
    padding: 16,
    gap: 8,
    alignItems: 'center',
  },
  input: {
    flex: 1,
    borderWidth: 1,
    borderRadius: 8,
    padding: 8,
    borderColor: '#ccc',
  }
})

export default function GoalsScreen() {
  const { toDoData, addTask, removeTask } = useToDo();
  const [inputText, setInputText] = useState('');

  return (
    <SafeAreaView style={styles.container}>

      {/* Back Button */}
      <View style={styles.backButton}>
        <TouchableOpacity onPress={() => router.back()}>
          <Ionicons name='arrow-back-circle-outline' size={40} color='black' />
        </TouchableOpacity>
      </View>

      <Text style={styles.headerText}>My Goals</Text>

      {/* Full To Do List */}
      <FlatList 
        data={toDoData}
        keyExtractor={(item) => item.id.toString()}
        renderItem={({item}) => 
          <View style={styles.toDoContainer}>
            <View style={styles.toDoInfoContainer}>
              <View style={{ flexDirection: 'row', alignItems: 'center', gap: 8 }}>
                <Checkbox value={item.isDone} />
                <Text style={item.isDone ? { textDecorationLine: 'line-through' } : {}}>
                {item.task}
                </Text >
              </View>
              <View>
                <TouchableOpacity onPress={() => removeTask(item.id)}>
                    <Ionicons name="trash-outline" size={24} color='black' />
                </TouchableOpacity>
              </View>
            </View>
          </View>
        }
      />

        {/* Add Task Input */}
        <View style={styles.inputRow}>
            <TextInput
            style={styles.input}
            placeholder="Add a new task..."
            value={inputText}
            onChangeText={setInputText}
            autoCorrect={false}
            />
            <TouchableOpacity onPress={() => {
                if (inputText.trim() === '') return;
                addTask(inputText);
                setInputText('');
            }}>
            <Ionicons name='add-circle' size={40} color='black' />
            </TouchableOpacity>
        </View>
      
    </SafeAreaView>
  );
}